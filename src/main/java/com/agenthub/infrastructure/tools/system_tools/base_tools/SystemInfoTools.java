package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Properties;

@AgentTools(name = "SystemInfoTools", description = "系统信息工具，提供操作系统、Java运行时、内存、处理器、系统属性等系统信息查询功能", defaultEnable = false)
public class SystemInfoTools {

    @Tool(name = "system_os_name", description = "Get operating system name")
    public String getOsName() {
        return System.getProperty("os.name");
    }

    @Tool(name = "system_os_version", description = "Get operating system version")
    public String getOsVersion() {
        return System.getProperty("os.version");
    }

    @Tool(name = "system_java_version", description = "Get Java runtime version")
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    @Tool(name = "system_java_home", description = "Get Java home directory path")
    public String getJavaHome() {
        return System.getProperty("java.home");
    }

    @Tool(name = "system_user_name", description = "Get current user name")
    public String getUserName() {
        return System.getProperty("user.name");
    }

    @Tool(name = "system_user_home", description = "Get user home directory")
    public String getUserHome() {
        return System.getProperty("user.home");
    }

    @Tool(name = "system_user_dir", description = "Get current working directory")
    public String getUserDir() {
        return System.getProperty("user.dir");
    }

    @Tool(name = "system_available_processors", description = "Get number of available processors")
    public int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Tool(name = "system_memory_max", description = "Get max memory in bytes")
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @Tool(name = "system_memory_total", description = "Get total memory in bytes")
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    @Tool(name = "system_memory_free", description = "Get free memory in bytes")
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Tool(name = "system_memory_used", description = "Get used memory in bytes")
    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    @Tool(name = "system_os_arch", description = "Get OS architecture")
    public String getOsArch() {
        return System.getProperty("os.arch");
    }

    @Tool(name = "system_file_separator", description = "Get file separator")
    public String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    @Tool(name = "system_line_separator", description = "Get line separator")
    public String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    @Tool(name = "system_path_separator", description = "Get path separator")
    public String getPathSeparator() {
        return System.getProperty("path.separator");
    }

    @Tool(name = "system_temp_dir", description = "Get temporary directory path")
    public String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    @Tool(name = "system_timezone", description = "Get system timezone")
    public String getTimezone() {
        return System.getProperty("user.timezone");
    }

    @Tool(name = "system_encoding", description = "Get file encoding")
    public String getFileEncoding() {
        return System.getProperty("file.encoding");
    }

    @Tool(name = "system_all_properties", description = "Get all system properties")
    public String getAllProperties() {
        Properties props = System.getProperties();
        StringBuilder sb = new StringBuilder();
        props.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        return sb.toString();
    }
}
