package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.ProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 运行时工具，提供命令执行、进程管理和代码执行功能。
 */
@RequiredArgsConstructor
@AgentTools(name = "RuntimeTools", description = "运行时工具，提供命令执行、进程管理和代码执行功能")
public class RuntimeTools {

    private static final long DEFAULT_TIMEOUT = 60;
    private static final List<String> BLOCKED = List.of(
            "rm -rf /", "mkfs", "dd if=", ":(){", "shutdown", "reboot"
    );

    @Tool(description = "执行系统命令并返回输出结果")
    public ProcessResult exec(
            @ToolParam(description = "要执行的命令") String command,
            @ToolParam(description = "超时时间（秒）") long timeout) {
        if (isBlocked(command)) return error("危险命令已被阻止");
        return executeProcess(new String[]{"bash", "-c", command}, timeout);
    }

    @Tool(description = "获取进程状态信息")
    public ProcessResult process(
            @ToolParam(description = "进程ID") String processId) {
        long pid = Long.parseLong(processId);
        var handle = ProcessHandle.of(pid).orElse(null);
        if (handle == null) return error("进程不存在");
        return ok(String.format("PID: %d, Alive: %b", handle.pid(), handle.isAlive()));
    }

    @Tool(description = "执行代码片段（支持Python和JavaScript）")
    public ProcessResult codeExecution(
            @ToolParam(description = "要执行的代码") String code,
            @ToolParam(description = "编程语言：python 或 javascript") String language) {
        String[] cmd = buildCommand(code, language);
        if (cmd == null) return error("不支持的语言: " + language);
        return executeProcess(cmd, DEFAULT_TIMEOUT);
    }

    private ProcessResult executeProcess(String[] cmd, long timeout) {
        long start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = readStream(process.getInputStream());
            boolean done = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!done) process.destroyForcibly();
            return buildResult(done, process.exitValue(), output, System.currentTimeMillis() - start);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private String[] buildCommand(String code, String language) {
        return switch (language.toLowerCase()) {
            case "python" -> new String[]{"python", "-c", code};
            case "javascript", "js", "node" -> new String[]{"node", "-e", code};
            default -> null;
        };
    }

    private boolean isBlocked(String command) {
        if (command == null) return false;
        String lower = command.toLowerCase();
        return BLOCKED.stream().anyMatch(lower::contains);
    }

    private String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
        }
    }

    private ProcessResult buildResult(boolean ok, int code, String out, long ms) {
        ProcessResult r = new ProcessResult();
        r.setSuccess(ok);
        r.setExitCode(code);
        r.setOutput(out);
        r.setExecutionTime(ms);
        return r;
    }

    private ProcessResult ok(String msg) { return buildResult(true, 0, msg, 0); }

    private ProcessResult error(String msg) { return buildResult(false, -1, msg, 0); }
}
