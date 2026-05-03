package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@AgentTools
public class ExecRuntimeTools {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<Long, ProcessInfo> processes = new ConcurrentHashMap<>();
    private final Path logDir = Paths.get(System.getProperty("user.home"), ".agenthub", "process_logs");

    public ExecRuntimeTools() throws IOException {
        Files.createDirectories(logDir);
    }

    // ==================== 同步执行 ====================

    @Tool(name = "exec_sync", description = "Execute command synchronously")
    public String execSync(String command) throws Exception {
        Process process = createProcessBuilder(command).start();
        return readOutput(process);
    }

    @Tool(name = "exec_sync_timeout", description = "Execute with timeout")
    public String execSyncTimeout(String command, int timeoutSec) throws Exception {
        Process process = createProcessBuilder(command).start();
        boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
        if (!finished) { process.destroyForcibly(); return "Timeout"; }
        return readOutput(process);
    }

    @Tool(name = "exec_sync_in_dir", description = "Execute in directory")
    public String execSyncInDir(String command, String dir) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command);
        pb.directory(new File(dir));
        return readOutput(pb.start());
    }

    @Tool(name = "exec_sync_with_env", description = "Execute with env vars")
    public String execSyncWithEnv(String command, String envVars) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command);
        setEnvironment(pb, envVars);
        return readOutput(pb.start());
    }

    // ==================== 流式执行 ====================

    @Tool(name = "exec_stream", description = "Execute and return Flux<String>")
    public Flux<String> execStream(String command) {
        return Flux.create(emitter -> {
            try {
                Process process = createProcessBuilder(command).start();
                BufferedReader reader = createReader(process.getInputStream());
                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.next(line);
                }
                process.waitFor();
                emitter.complete();
            } catch (Exception e) {
                emitter.error(e);
            }
        });
    }

    @Tool(name = "exec_stream_to_file", description = "Stream output to file")
    public String execStreamToFile(String command, String filePath) throws Exception {
        Process process = createProcessBuilder(command).start();
        Path logFile = Paths.get(filePath);
        try (BufferedReader reader = createReader(process.getInputStream());
             BufferedWriter writer = Files.newBufferedWriter(logFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
                writer.flush();
            }
        }
        process.waitFor();
        return "Output saved to: " + filePath;
    }

    @Tool(name = "exec_stream_lines", description = "Stream first N lines")
    public Flux<String> execStreamLines(String command, int maxLines) {
        return Flux.create(emitter -> {
            try {
                Process process = createProcessBuilder(command).start();
                BufferedReader reader = createReader(process.getInputStream());
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < maxLines) {
                    emitter.next(line);
                    count++;
                }
                process.destroyForcibly();
                emitter.complete();
            } catch (Exception e) {
                emitter.error(e);
            }
        });
    }

    // ==================== 异步执行 ====================

    @Tool(name = "exec_async", description = "Execute async, return PID")
    public String execAsync(String command) throws Exception {
        return execAsyncNamed(command, "process_" + System.currentTimeMillis());
    }

    @Tool(name = "exec_async_named", description = "Execute async with name")
    public String execAsyncNamed(String command, String name) throws Exception {
        Process process = createProcessBuilder(command).start();
        long pid = process.pid();
        Path logFile = logDir.resolve(name + "_" + pid + ".log");
        ProcessInfo info = new ProcessInfo(pid, name, command, logFile, process);
        processes.put(pid, info);
        startOutputWriter(process, logFile, info);
        return "PID: " + pid + ", Log: " + logFile.getFileName();
    }

    @Tool(name = "exec_async_in_dir", description = "Execute async in directory")
    public String execAsyncInDir(String command, String dir, String name) throws Exception {
        ProcessBuilder pb = createProcessBuilder(command);
        pb.directory(new File(dir));
        Process process = pb.start();
        long pid = process.pid();
        Path logFile = logDir.resolve(name + "_" + pid + ".log");
        ProcessInfo info = new ProcessInfo(pid, name, command, logFile, process);
        processes.put(pid, info);
        startOutputWriter(process, logFile, info);
        return "PID: " + pid;
    }

    // ==================== 日志管理 ====================

    @Tool(name = "process_get_log", description = "Get process log by PID")
    public String processGetLog(long pid) throws IOException {
        ProcessInfo info = processes.get(pid);
        if (info != null && Files.exists(info.logFile)) return Files.readString(info.logFile);
        return findAndReadLog(pid);
    }

    @Tool(name = "process_get_log_by_name", description = "Get log by process name")
    public String processGetLogByName(String name) throws IOException {
        Optional<Path> logFile = findLogByName(name);
        return logFile.isPresent() ? Files.readString(logFile.get()) : "Log not found";
    }

    @Tool(name = "process_tail_log", description = "Get last N lines of log")
    public String processTailLog(long pid, int lines) throws IOException {
        ProcessInfo info = processes.get(pid);
        if (info == null) return "Process not found";
        return readLastLines(info.logFile, lines);
    }

    @Tool(name = "process_list_logs", description = "List all log files")
    public String processListLogs() throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(logDir).filter(p -> p.toString().endsWith(".log"))
            .sorted(Comparator.reverseOrder())
            .forEach(p -> sb.append(p.getFileName()).append("\n"));
        return sb.toString();
    }

    @Tool(name = "process_delete_log", description = "Delete log file")
    public String processDeleteLog(long pid) throws IOException {
        ProcessInfo info = processes.get(pid);
        if (info != null) { Files.deleteIfExists(info.logFile); return "Deleted"; }
        Optional<Path> logFile = findLogByPid(pid);
        if (logFile.isPresent()) { Files.delete(logFile.get()); return "Deleted"; }
        return "Log not found";
    }

    // ==================== 进程管理 ====================

    @Tool(name = "process_list", description = "List all processes")
    public String processList() throws Exception {
        StringBuilder sb = new StringBuilder("=== Managed ===\n");
        processes.forEach((pid, info) -> sb.append(formatProcessInfo(pid, info)).append("\n"));
        sb.append("\n=== System ===\n").append(execSync(getListCommand()));
        return sb.toString();
    }

    @Tool(name = "process_status", description = "Get process status")
    public String processStatus(long pid) throws Exception {
        ProcessInfo info = processes.get(pid);
        if (info != null) return formatDetailedStatus(info);
        return execSync(getStatusCommand(pid));
    }

    @Tool(name = "process_kill", description = "Kill process")
    public String processKill(long pid) throws Exception {
        ProcessInfo info = processes.get(pid);
        if (info != null) { info.process.destroyForcibly(); processes.remove(pid); return "Killed"; }
        return execSync(getKillCommand(pid));
    }

    @Tool(name = "process_wait", description = "Wait for process")
    public String processWait(long pid) throws Exception {
        ProcessInfo info = processes.get(pid);
        if (info == null) return "Not found";
        int code = info.process.waitFor();
        return "Exit code: " + code;
    }

    @Tool(name = "process_is_alive", description = "Check if alive")
    public boolean processIsAlive(long pid) {
        ProcessInfo info = processes.get(pid);
        return info != null && info.process.isAlive();
    }

    // ==================== 代码执行 ====================

    @Tool(name = "code_exec_sync", description = "Execute code synchronously")
    public String codeExecSync(String lang, String code) throws Exception {
        Path file = createCodeFile(lang, code);
        String result = execSync(buildCodeCommand(lang, file));
        Files.deleteIfExists(file);
        return result;
    }

    @Tool(name = "code_exec_async", description = "Execute code asynchronously")
    public String codeExecAsync(String lang, String code) throws Exception {
        Path file = createCodeFile(lang, code);
        return execAsyncNamed(buildCodeCommand(lang, file), lang + "_code");
    }

    @Tool(name = "code_exec_stream", description = "Execute code with stream")
    public Flux<String> codeExecStream(String lang, String code) throws Exception {
        Path file = createCodeFile(lang, code);
        return execStream(buildCodeCommand(lang, file));
    }

    // ==================== 私有方法 ====================

    private ProcessBuilder createProcessBuilder(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? new ProcessBuilder("cmd.exe", "/c", command) 
                                  : new ProcessBuilder("sh", "-c", command);
    }

    private String readOutput(Process process) throws IOException {
        BufferedReader reader = createReader(process.getInputStream());
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");
        return output.toString();
    }

    private BufferedReader createReader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    private void setEnvironment(ProcessBuilder pb, String envVars) {
        for (String env : envVars.split(";")) {
            String[] parts = env.split("=", 2);
            if (parts.length == 2) pb.environment().put(parts[0].trim(), parts[1].trim());
        }
    }

    private void startOutputWriter(Process process, Path logFile, ProcessInfo info) {
        executor.submit(() -> {
            try (BufferedReader reader = createReader(process.getInputStream());
                 BufferedWriter writer = Files.newBufferedWriter(logFile, StandardOpenOption.CREATE)) {
                writeHeader(writer, info);
                String line;
                while ((line = reader.readLine()) != null) writer.write(line + "\n");
                writeFooter(writer, process.waitFor());
                info.status = "completed";
            } catch (Exception e) { info.status = "failed"; }
        });
    }

    private void writeHeader(BufferedWriter writer, ProcessInfo info) throws IOException {
        writer.write("=== Started ===\nPID: " + info.pid + "\nCommand: " + info.command + "\n\n");
    }

    private void writeFooter(BufferedWriter writer, int code) throws IOException {
        writer.write("\n=== Finished ===\nExit: " + code + "\n");
    }

    private String findAndReadLog(long pid) throws IOException {
        Optional<Path> logFile = findLogByPid(pid);
        return logFile.isPresent() ? Files.readString(logFile.get()) : "Log not found";
    }

    private Optional<Path> findLogByPid(long pid) throws IOException {
        return Files.list(logDir).filter(p -> p.getFileName().toString().contains("_" + pid + ".log")).findFirst();
    }

    private Optional<Path> findLogByName(String name) throws IOException {
        return Files.list(logDir).filter(p -> p.getFileName().toString().startsWith(name + "_")).findFirst();
    }

    private String readLastLines(Path file, int lines) throws IOException {
        List<String> all = Files.readAllLines(file);
        int start = Math.max(0, all.size() - lines);
        return String.join("\n", all.subList(start, all.size()));
    }

    private String formatProcessInfo(long pid, ProcessInfo info) {
        return pid + ": " + info.name + " [" + info.status + "]";
    }

    private String formatDetailedStatus(ProcessInfo info) {
        return "PID: " + info.pid + "\nName: " + info.name + "\nStatus: " + info.status + "\nLog: " + info.logFile;
    }

    private String getListCommand() {
        return System.getProperty("os.name").toLowerCase().contains("win") ? "tasklist" : "ps -e";
    }

    private String getStatusCommand(long pid) {
        return System.getProperty("os.name").toLowerCase().contains("win") 
            ? "tasklist /FI \"PID eq " + pid + "\"" : "ps -p " + pid;
    }

    private String getKillCommand(long pid) {
        return System.getProperty("os.name").toLowerCase().contains("win") 
            ? "taskkill /F /PID " + pid : "kill -9 " + pid;
    }

    private Path createCodeFile(String lang, String code) throws IOException {
        String ext = switch (lang.toLowerCase()) {
            case "python" -> ".py"; case "node", "javascript" -> ".js";
            case "ruby" -> ".rb"; case "perl" -> ".pl"; case "bash" -> ".sh";
            default -> ".txt";
        };
        Path file = Files.createTempFile("script", ext);
        Files.writeString(file, code);
        return file;
    }

    private String buildCodeCommand(String lang, Path file) {
        return switch (lang.toLowerCase()) {
            case "python" -> "python3 " + file;
            case "node", "javascript" -> "node " + file;
            case "ruby" -> "ruby " + file;
            case "perl" -> "perl " + file;
            case "bash" -> "bash " + file;
            default -> "cat " + file;
        };
    }

    private static class ProcessInfo {
        final long pid;
        final String name, command;
        final Path logFile;
        final Process process;
        volatile String status = "running";

        ProcessInfo(long pid, String name, String command, Path logFile, Process process) {
            this.pid = pid; this.name = name; this.command = command;
            this.logFile = logFile; this.process = process;
        }
    }
}
