package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@AgentTools(name = "CommandLineTools", description = "命令行工具，提供系统命令执行、进程管理、系统信息查询等命令行操作功能", defaultEnable = false)
public class CommandLineTools {

    @Tool(name = "cmd_exec", description = "Execute command and return output")
    public String exec(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");
        process.waitFor();
        return output.toString();
    }

    @Tool(name = "cmd_exec_with_timeout", description = "Execute command with timeout in seconds")
    public String execWithTimeout(String command, int timeoutSeconds) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");
        boolean finished = process.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) process.destroyForcibly();
        return finished ? output.toString() : "Command timed out";
    }

    @Tool(name = "cmd_exec_in_dir", description = "Execute command in specific directory")
    public String execInDir(String command, String workingDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
        pb.directory(new java.io.File(workingDir));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");
        process.waitFor();
        return output.toString();
    }

    @Tool(name = "cmd_which", description = "Find command location (Unix-like)")
    public String which(String command) throws Exception {
        return exec("which " + command).trim();
    }

    @Tool(name = "cmd_where", description = "Find command location (Windows)")
    public String where(String command) throws Exception {
        return exec("where " + command).trim();
    }

    @Tool(name = "cmd_exit_code", description = "Execute and return exit code")
    public int exitCode(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command.split("\\s+"));
        Process process = pb.start();
        return process.waitFor();
    }

    @Tool(name = "cmd_list_processes", description = "List running processes")
    public String listProcesses() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("tasklist");
        return exec("ps aux");
    }

    @Tool(name = "cmd_kill_process", description = "Kill process by PID")
    public String killProcess(int pid) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("taskkill /F /PID " + pid);
        return exec("kill -9 " + pid);
    }

    @Tool(name = "cmd_disk_usage", description = "Get disk usage")
    public String diskUsage() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("wmic logicaldisk get size,freespace,caption");
        return exec("df -h");
    }

    @Tool(name = "cmd_memory_info", description = "Get memory information")
    public String memoryInfo() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /value");
        return exec("free -h");
    }

    @Tool(name = "cmd_cpu_info", description = "Get CPU information")
    public String cpuInfo() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("wmic cpu get name,numberofcores,maxclockspeed");
        return exec("lscpu");
    }

    @Tool(name = "cmd_network_connections", description = "List network connections")
    public String networkConnections() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("netstat -an");
        return exec("netstat -tulpn");
    }

    @Tool(name = "cmd_env_vars", description = "List environment variables")
    public String envVars() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("set");
        return exec("env");
    }

    @Tool(name = "cmd_uptime", description = "Get system uptime")
    public String uptime() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("wmic os get lastbootuptime");
        return exec("uptime");
    }

    @Tool(name = "cmd_whoami", description = "Get current user")
    public String whoami() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("whoami");
        return exec("whoami");
    }

    @Tool(name = "cmd_hostname", description = "Get hostname")
    public String hostname() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return exec("hostname");
        return exec("hostname");
    }
}
