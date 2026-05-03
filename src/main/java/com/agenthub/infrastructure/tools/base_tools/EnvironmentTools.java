package com.agenthub.infrastructure.tools.base_tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Map;

@AgentTools(defaultEnable = false)
public class EnvironmentTools {

    @Tool(name = "env_get", description = "Get environment variable value")
    public String get(String name) {
        return System.getenv(name);
    }

    @Tool(name = "env_get_default", description = "Get env var with default value")
    public String getOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null ? value : defaultValue;
    }

    @Tool(name = "env_all", description = "Get all environment variables")
    public String getAll() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> env = System.getenv();
        env.forEach((k, v) -> sb.append(k).append("=").append(v).append("\n"));
        return sb.toString();
    }

    @Tool(name = "env_exists", description = "Check if environment variable exists")
    public boolean exists(String name) {
        return System.getenv(name) != null;
    }

    @Tool(name = "env_path", description = "Get PATH environment variable")
    public String getPath() {
        return System.getenv("PATH");
    }

    @Tool(name = "env_home", description = "Get HOME environment variable")
    public String getHome() {
        return System.getenv("HOME");
    }

    @Tool(name = "env_user", description = "Get USER environment variable")
    public String getUser() {
        return System.getenv("USER");
    }

    @Tool(name = "env_lang", description = "Get LANG environment variable")
    public String getLang() {
        return System.getenv("LANG");
    }

    @Tool(name = "env_pwd", description = "Get PWD environment variable")
    public String getPwd() {
        return System.getenv("PWD");
    }

    @Tool(name = "env_shell", description = "Get SHELL environment variable")
    public String getShell() {
        return System.getenv("SHELL");
    }

    @Tool(name = "env_java_home", description = "Get JAVA_HOME environment variable")
    public String getJavaHome() {
        return System.getenv("JAVA_HOME");
    }

    @Tool(name = "env_path_separator", description = "Get path separator for current OS")
    public String getPathSeparator() {
        return System.getProperty("path.separator");
    }

    @Tool(name = "env_search_paths", description = "Get PATH as array")
    public String getSearchPaths() {
        String path = System.getenv("PATH");
        String separator = System.getProperty("path.separator");
        return String.join("\n", path.split(separator));
    }
}
