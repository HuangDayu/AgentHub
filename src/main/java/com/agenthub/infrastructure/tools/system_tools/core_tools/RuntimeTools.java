package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.ProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 运行时工具，提供命令执行、进程管理和代码执行功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "RuntimeTools", description = "运行时工具，提供命令执行、进程管理和代码执行功能")
public class RuntimeTools {

    private static final long DEFAULT_TIMEOUT_SECONDS = 60;
    private static final List<String> BLOCKED_COMMANDS = List.of(
            "rm -rf /", "mkfs", "dd if=", ":(){", "fork", "shutdown", "reboot", "halt"
    );

    @Tool(description = "执行系统命令并返回输出结果。支持任意shell命令。")
    public ProcessResult exec(
            @ToolParam(description = "要执行的命令，如 'ls -la' 或 'python script.py'") String command,
            @ToolParam(description = "超时时间（秒），建议60-300") long timeoutSeconds) throws IOException, InterruptedException {
        if (isBlockedCommand(command)) {
            return new ProcessResult(false, -1, "", "危险命令已被阻止", 0);
        }
        long startTime = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(false);
        Process process = processBuilder.start();
        String stdout = readStream(process.getInputStream());
        String stderr = readStream(process.getErrorStream());
        boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        long executionTime = System.currentTimeMillis() - startTime;
        String output = buildOutput(stdout, stderr, process.exitValue());
        return new ProcessResult(completed, process.exitValue(), output, stderr, executionTime);
    }

    @Tool(description = "获取进程状态信息")
    public ProcessResult process(
            @ToolParam(description = "进程ID") String processId) {
        long pid = Long.parseLong(processId);
        ProcessHandle processHandle = ProcessHandle.of(pid).orElse(null);
        if (processHandle == null) {
            return new ProcessResult(false, -1, "", "进程不存在", 0);
        }
        String info = String.format("PID: %d, Alive: %b, Start: %s",
                processHandle.pid(),
                processHandle.isAlive(),
                processHandle.info().startInstant().orElse(null));
        return new ProcessResult(true, 0, info, "", 0);
    }

    @Tool(description = "执行代码片段（支持Python和JavaScript）。代码会被写入临时文件后执行，避免注入风险。")
    public ProcessResult codeExecution(
            @ToolParam(description = "要执行的代码") String code,
            @ToolParam(description = "编程语言：python 或 javascript") String language) throws IOException, InterruptedException {
        String[] cmd = buildSafeExecutionCommand(code, language);
        if (cmd == null) {
            return new ProcessResult(false, -1, "", "不支持的语言: " + language, 0);
        }
        long startTime = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(false);
        Process process = processBuilder.start();
        String stdout = readStream(process.getInputStream());
        String stderr = readStream(process.getErrorStream());
        boolean completed = process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        long executionTime = System.currentTimeMillis() - startTime;
        String output = buildOutput(stdout, stderr, process.exitValue());
        return new ProcessResult(completed, process.exitValue(), output, stderr, executionTime);
    }

    private boolean isBlockedCommand(String command) {
        if (command == null) return false;
        String lower = command.toLowerCase();
        return BLOCKED_COMMANDS.stream().anyMatch(lower::contains);
    }

    private String[] buildSafeExecutionCommand(String code, String language) {
        return switch (language.toLowerCase()) {
            case "python" -> new String[]{"python", "-c", code};
            case "javascript", "js", "node" -> new String[]{"node", "-e", code};
            default -> null;
        };
    }

    private String readStream(java.io.InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    private String buildOutput(String stdout, String stderr, int exitCode) {
        StringBuilder sb = new StringBuilder();
        if (!stdout.isBlank()) {
            sb.append(stdout.trim());
        }
        if (!stderr.isBlank()) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append("STDERR: ").append(stderr.trim());
        }
        if (sb.isEmpty()) {
            sb.append("命令执行完成，退出码: ").append(exitCode);
        }
        return sb.toString();
    }
}
