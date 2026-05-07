package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getWorkspace;

@AgentTools(name = "ManagementTools", description = "Agent管理工具，提供Agent的创建、启动、停止、状态管理、消息发送等功能", defaultEnable = false)
public class ManagementTools {

    private final Map<String, Process> runningAgents = new ConcurrentHashMap<>();

    @Tool(name = "agents_list", description = "List all agents")
    public String agentsList(ToolContext toolContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(getWorkspace(toolContext).getAgentsPath())
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    try {
                        String json = Files.readString(p);
                        String id = p.getFileName().toString().replace(".json", "");
                        String name = json.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1");
                        String status = json.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
                        sb.append(id).append(": ").append(name).append(" (").append(status).append(")\n");
                    } catch (Exception e) {
                    }
                });
        return sb.toString();
    }

    @Tool(name = "agents_create", description = "Create a new agent")
    public String agentsCreate(String name, String type, ToolContext toolContext) throws IOException {
        String id = UUID.randomUUID().toString();
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(id + ".json");

        String json = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"type\":\"%s\",\"status\":\"idle\",\"created\":\"%s\"}",
                id, name, type, Instant.now().toString());
        Files.writeString(agentFile, json);

        return "Created agent: " + id;
    }

    @Tool(name = "agents_get", description = "Get agent info")
    public String agentsGet(String agentId, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";
        return Files.readString(agentFile);
    }

    @Tool(name = "agents_status", description = "Get agent status")
    public String agentsStatus(String agentId, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";
        String json = Files.readString(agentFile);
        return json.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
    }

    @Tool(name = "agents_start", description = "Start an agent")
    public String agentsStart(String agentId, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";

        String json = Files.readString(agentFile);
        String type = json.replaceAll(".*\"type\":\"([^\"]+)\".*", "$1");

        // Start agent process based on type
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", ".", "AgentMain", agentId, type);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        runningAgents.put(agentId, process);

        updateAgentStatus(agentId, "running", toolContext);
        return "Started agent: " + agentId + " (PID: " + process.pid() + ")";
    }

    @Tool(name = "agents_stop", description = "Stop an agent")
    public String agentsStop(String agentId, ToolContext toolContext) throws IOException {
        Process process = runningAgents.remove(agentId);
        if (process != null) {
            process.destroyForcibly();
            updateAgentStatus(agentId, "stopped", toolContext);
            return "Stopped agent: " + agentId;
        }
        return "Agent not running";
    }

    @Tool(name = "agents_pause", description = "Pause an agent")
    public String agentsPause(String agentId, ToolContext toolContext) throws IOException {
        Process process = runningAgents.get(agentId);
        if (process == null) return "Agent not running";

        // Send SIGSTOP to pause
        ProcessBuilder pb = new ProcessBuilder("kill", "-STOP", String.valueOf(process.pid()));
        pb.start();
        updateAgentStatus(agentId, "paused", toolContext);
        return "Paused agent: " + agentId;
    }

    @Tool(name = "agents_resume", description = "Resume a paused agent")
    public String agentsResume(String agentId, ToolContext toolContext) throws IOException {
        Process process = runningAgents.get(agentId);
        if (process == null) return "Agent not running";

        // Send SIGCONT to resume
        ProcessBuilder pb = new ProcessBuilder("kill", "-CONT", String.valueOf(process.pid()));
        pb.start();
        updateAgentStatus(agentId, "running", toolContext);
        return "Resumed agent: " + agentId;
    }

    @Tool(name = "agents_delete", description = "Delete an agent")
    public String agentsDelete(String agentId, ToolContext toolContext) throws IOException {
        agentsStop(agentId, toolContext);
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";
        Files.delete(agentFile);
        return "Deleted agent: " + agentId;
    }

    @Tool(name = "agents_assign_task", description = "Assign task to agent")
    public String agentsAssignTask(String agentId, String task, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";

        String json = Files.readString(agentFile);
        json = json.replaceFirst("\\}$", ",\"task\":\"" + task.replace("\"", "\\\"") + "\"}");
        Files.writeString(agentFile, json);

        // Write task to agent's inbox
        Path inbox = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".inbox");
        Files.writeString(inbox, task + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        return "Assigned task to " + agentId + ": " + task;
    }

    @Tool(name = "agents_get_task", description = "Get agent's current task")
    public String agentsGetTask(String agentId, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return "Agent not found";

        String json = Files.readString(agentFile);
        if (!json.contains("\"task\":")) return "No task assigned";
        return json.replaceAll(".*\"task\":\"([^\"]+)\".*", "$1");
    }

    @Tool(name = "agents_count", description = "Count agents")
    public int agentsCount(ToolContext toolContext) throws IOException {
        return (int) Files.list(getWorkspace(toolContext).getAgentsPath()).filter(p -> p.toString().endsWith(".json")).count();
    }

    @Tool(name = "agents_count_by_status", description = "Count agents by status")
    public int agentsCountByStatus(String status, ToolContext toolContext) throws IOException {
        return (int) Files.list(getWorkspace(toolContext).getAgentsPath())
                .filter(p -> p.toString().endsWith(".json"))
                .filter(p -> {
                    try {
                        return Files.readString(p).contains("\"status\":\"" + status + "\"");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
    }

    @Tool(name = "agents_list_by_type", description = "List agents by type")
    public String agentsListByType(String type, ToolContext toolContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(getWorkspace(toolContext).getAgentsPath())
                .filter(p -> p.toString().endsWith(".json"))
                .filter(p -> {
                    try {
                        return Files.readString(p).contains("\"type\":\"" + type + "\"");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(p -> {
                    try {
                        String id = p.getFileName().toString().replace(".json", "");
                        sb.append(id).append("\n");
                    } catch (Exception e) {
                    }
                });
        return sb.length() > 0 ? sb.toString() : "No agents of type: " + type;
    }

    @Tool(name = "message", description = "Send message to agent")
    public String message(String agentId, String message, ToolContext toolContext) throws IOException {
        Path inbox = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".inbox");
        if (!Files.exists(getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json")))
            return "Agent not found";

        String entry = String.format("[%s] %s\n", Instant.now().toString(), message);
        Files.writeString(inbox, entry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        return "Message sent to " + agentId;
    }

    @Tool(name = "message_broadcast", description = "Broadcast message to all agents")
    public String messageBroadcast(String message, ToolContext toolContext) throws IOException {
        int count = 0;
        for (Path p : Files.list(getWorkspace(toolContext).getAgentsPath()).filter(p -> p.toString().endsWith(".json")).toList()) {
            String id = p.getFileName().toString().replace(".json", "");
            message(id, message, toolContext);
            count++;
        }
        return "Broadcast to " + count + " agents";
    }

    private void updateAgentStatus(String agentId, String status, ToolContext toolContext) throws IOException {
        Path agentFile = getWorkspace(toolContext).getAgentsPath().resolve(agentId + ".json");
        if (!Files.exists(agentFile)) return;

        String json = Files.readString(agentFile);
        json = json.replaceAll("\"status\":\"[^\"]+\"", "\"status\":\"" + status + "\"");
        Files.writeString(agentFile, json);
    }
}
