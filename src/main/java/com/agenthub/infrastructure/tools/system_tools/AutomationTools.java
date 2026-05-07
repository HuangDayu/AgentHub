package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getWorkspace;

@AgentTools(name = "AutomationTools", description = "自动化任务工具，提供定时任务(cron)的创建、管理、执行等功能")
public class AutomationTools {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    @Tool(name = "cron_create", description = "Create cron job")
    public String cronCreate(String name, String schedule, String command, ToolContext toolContext) throws IOException {
        String id = UUID.randomUUID().toString();
        Path jobFile = getWorkspace(toolContext).getCronPath().resolve(id + ".json");

        String json = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"schedule\":\"%s\",\"command\":\"%s\",\"enabled\":true,\"created\":\"%s\"}",
                id, name, schedule, command.replace("\"", "\\\""), Instant.now().toString());
        Files.writeString(jobFile, json);

        scheduleJob(id, schedule, command);
        return "Created cron job: " + id;
    }

    @Tool(name = "cron_list", description = "List all cron jobs")
    public String cronList(ToolContext toolContext) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(getWorkspace(toolContext).getCronPath())
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    try {
                        String json = Files.readString(p);
                        sb.append(p.getFileName().toString().replace(".json", "")).append(": ")
                                .append(json.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1"))
                                .append(" [").append(json.replaceAll(".*\"schedule\":\"([^\"]+)\".*", "$1")).append("]\n");
                    } catch (Exception e) {
                    }
                });
        return sb.toString();
    }

    @Tool(name = "cron_get", description = "Get cron job details")
    public String cronGet(String jobId, ToolContext toolContext) throws IOException {
        Path jobFile = getWorkspace(toolContext).getCronPath().resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";
        return Files.readString(jobFile);
    }

    @Tool(name = "cron_enable", description = "Enable cron job")
    public String cronEnable(String jobId, ToolContext toolContext) throws IOException {
        return updateJobStatus(jobId, true, toolContext);
    }

    @Tool(name = "cron_disable", description = "Disable cron job")
    public String cronDisable(String jobId, ToolContext toolContext) throws IOException {
        return updateJobStatus(jobId, false, toolContext);
    }

    @Tool(name = "cron_delete", description = "Delete cron job")
    public String cronDelete(String jobId, ToolContext toolContext) throws IOException {
        Path jobFile = getWorkspace(toolContext).getCronPath().resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";

        ScheduledFuture<?> future = scheduledJobs.remove(jobId);
        if (future != null) future.cancel(false);

        Files.delete(jobFile);
        return "Deleted: " + jobId;
    }

    @Tool(name = "cron_run", description = "Run cron job now")
    public String cronRun(String jobId, ToolContext toolContext) throws IOException {
        Path jobFile = getWorkspace(toolContext).getCronPath().resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";

        String json = Files.readString(jobFile);
        String command = json.replaceAll(".*\"command\":\"([^\"]+)\".*", "$1");

        ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());

        return "Executed: " + output;
    }

    @Tool(name = "cron_count", description = "Count cron jobs")
    public int cronCount(ToolContext toolContext) throws IOException {
        return (int) Files.list(getWorkspace(toolContext).getCronPath()).filter(p -> p.toString().endsWith(".json")).count();
    }


    private String updateJobStatus(String jobId, boolean enabled, ToolContext toolContext) throws IOException {
        Path jobFile = getWorkspace(toolContext).getCronPath().resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";

        String json = Files.readString(jobFile);
        json = json.replaceAll("\"enabled\":[^,}]+", "\"enabled\":" + enabled);
        Files.writeString(jobFile, json);

        if (enabled) {
            String schedule = json.replaceAll(".*\"schedule\":\"([^\"]+)\".*", "$1");
            String command = json.replaceAll(".*\"command\":\"([^\"]+)\".*", "$1");
            scheduleJob(jobId, schedule, command);
        } else {
            ScheduledFuture<?> future = scheduledJobs.remove(jobId);
            if (future != null) future.cancel(false);
        }

        return (enabled ? "Enabled" : "Disabled") + ": " + jobId;
    }

    private void scheduleJob(String id, String schedule, String command) {
        long delay = parseSchedule(schedule);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
                pb.start();
            } catch (Exception e) {
            }
        }, delay, delay, TimeUnit.MILLISECONDS);
        scheduledJobs.put(id, future);
    }

    private long parseSchedule(String schedule) {
        // Simple cron-like parsing: "*/5 * * * *" -> 5 minutes
        if (schedule.startsWith("*/")) {
            int minutes = Integer.parseInt(schedule.split("\\s+")[0].substring(2));
            return minutes * 60 * 1000L;
        }
        return 60 * 1000L; // Default 1 minute
    }
}
