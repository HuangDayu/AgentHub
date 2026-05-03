package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@AgentTools
public class AutomationTools {

    private final Path cronDir = Paths.get(System.getProperty("user.home"), ".agenthub", "cron");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    public AutomationTools() throws IOException {
        Files.createDirectories(cronDir);
    }

    @Tool(name = "cron_create", description = "Create cron job")
    public String cronCreate(String name, String schedule, String command) throws IOException {
        String id = UUID.randomUUID().toString();
        Path jobFile = cronDir.resolve(id + ".json");

        String json = String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"schedule\":\"%s\",\"command\":\"%s\",\"enabled\":true,\"created\":\"%s\"}",
                id, name, schedule, command.replace("\"", "\\\""), Instant.now().toString());
        Files.writeString(jobFile, json);

        scheduleJob(id, schedule, command);
        return "Created cron job: " + id;
    }

    @Tool(name = "cron_list", description = "List all cron jobs")
    public String cronList() throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(cronDir)
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
    public String cronGet(String jobId) throws IOException {
        Path jobFile = cronDir.resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";
        return Files.readString(jobFile);
    }

    @Tool(name = "cron_enable", description = "Enable cron job")
    public String cronEnable(String jobId) throws IOException {
        return updateJobStatus(jobId, true);
    }

    @Tool(name = "cron_disable", description = "Disable cron job")
    public String cronDisable(String jobId) throws IOException {
        return updateJobStatus(jobId, false);
    }

    @Tool(name = "cron_delete", description = "Delete cron job")
    public String cronDelete(String jobId) throws IOException {
        Path jobFile = cronDir.resolve(jobId + ".json");
        if (!Files.exists(jobFile)) return "Job not found";

        ScheduledFuture<?> future = scheduledJobs.remove(jobId);
        if (future != null) future.cancel(false);

        Files.delete(jobFile);
        return "Deleted: " + jobId;
    }

    @Tool(name = "cron_run", description = "Run cron job now")
    public String cronRun(String jobId) throws IOException {
        Path jobFile = cronDir.resolve(jobId + ".json");
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
    public int cronCount() throws IOException {
        return (int) Files.list(cronDir).filter(p -> p.toString().endsWith(".json")).count();
    }


    private String updateJobStatus(String jobId, boolean enabled) throws IOException {
        Path jobFile = cronDir.resolve(jobId + ".json");
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
