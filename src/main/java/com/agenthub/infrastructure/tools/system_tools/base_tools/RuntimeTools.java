package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.base_tools.dto.ProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@AgentTools(name = "RuntimeTools", description = "运行时工具，提供命令执行、进程管理和代码执行功能")
public class RuntimeTools {

    @Tool(description = "执行系统命令")
    public ProcessResult exec(@ToolParam String command, @ToolParam long timeoutSeconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        Process process = processBuilder.start();
        String output = readProcessOutput(process);
        boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        long executionTime = System.currentTimeMillis() - startTime;
        return new ProcessResult(completed, process.exitValue(), output, "", executionTime);
    }

    @Tool(description = "管理进程，获取进程状态")
    public ProcessResult process(@ToolParam String processId) {
        long pid = Long.parseLong(processId);
        ProcessHandle processHandle = ProcessHandle.of(pid).orElse(null);
        if (processHandle == null) return new ProcessResult(false, -1, "", "进程不存在", 0);
        String info = String.format("PID: %d, Alive: %b", processHandle.pid(), processHandle.isAlive());
        return new ProcessResult(true, 0, info, "", 0);
    }

    @Tool(description = "执行代码片段")
    public ProcessResult codeExecution(@ToolParam String code, @ToolParam String language) throws IOException, InterruptedException {
        String command = buildExecutionCommand(code, language);
        return exec(command, 30);
    }

    private String readProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");
        return output.toString();
    }

    private String buildExecutionCommand(String code, String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "python -c \"" + code + "\"";
            case "javascript" -> "node -e \"" + code + "\"";
            default -> code;
        };
    }
}
