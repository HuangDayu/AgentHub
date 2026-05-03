package com.agenthub.infrastructure.tools;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@AgentTools
public class SessionTools {

    private final Path sessionDir = Paths.get(System.getProperty("user.home"), ".agenthub", "sessions");
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public SessionTools() throws IOException {
        Files.createDirectories(sessionDir);
        loadSessions();
    }

    @Tool(name = "sessions_list", description = "List all sessions")
    public String sessionsList() throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(sessionDir)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                String id = dir.getFileName().toString();
                try {
                    String meta = Files.readString(dir.resolve("meta.json"));
                    sb.append(id).append(": ").append(meta).append("\n");
                } catch (Exception e) {
                    sb.append(id).append(": [error reading metadata]\n");
                }
            });
        return sb.toString();
    }

    @Tool(name = "sessions_create", description = "Create new session")
    public String sessionsCreate(String name) throws IOException {
        String id = UUID.randomUUID().toString();
        Path sessionPath = sessionDir.resolve(id);
        Files.createDirectories(sessionPath);
        
        String meta = String.format("{\"name\":\"%s\",\"created\":\"%s\",\"status\":\"active\"}", 
            name, Instant.now().toString());
        Files.writeString(sessionPath.resolve("meta.json"), meta);
        Files.createFile(sessionPath.resolve("history.log"));
        
        activeSessions.put(id, new Session(id, name));
        return "Created session: " + id;
    }

    @Tool(name = "sessions_get", description = "Get session info")
    public String sessionsGet(String sessionId) throws IOException {
        Path metaPath = sessionDir.resolve(sessionId).resolve("meta.json");
        if (!Files.exists(metaPath)) return "Session not found";
        return Files.readString(metaPath);
    }

    @Tool(name = "sessions_status", description = "Get session status")
    public String sessionsStatus(String sessionId) throws IOException {
        Path metaPath = sessionDir.resolve(sessionId).resolve("meta.json");
        if (!Files.exists(metaPath)) return "Session not found";
        String meta = Files.readString(metaPath);
        return meta.replaceAll(".*\"status\":\"([^\"]+)\".*", "$1");
    }

    @Tool(name = "sessions_activate", description = "Activate session")
    public String sessionsActivate(String sessionId) throws IOException {
        return updateStatus(sessionId, "active");
    }

    @Tool(name = "sessions_deactivate", description = "Deactivate session")
    public String sessionsDeactivate(String sessionId) throws IOException {
        return updateStatus(sessionId, "inactive");
    }

    @Tool(name = "sessions_delete", description = "Delete session")
    public String sessionsDelete(String sessionId) throws IOException {
        Path sessionPath = sessionDir.resolve(sessionId);
        if (!Files.exists(sessionPath)) return "Session not found";
        
        Files.walk(sessionPath)
            .sorted(Comparator.reverseOrder())
            .forEach(p -> { try { Files.delete(p); } catch (Exception e) {} });
        
        activeSessions.remove(sessionId);
        return "Deleted session: " + sessionId;
    }

    @Tool(name = "sessions_history", description = "Get session history")
    public String sessionsHistory(String sessionId) throws IOException {
        Path historyPath = sessionDir.resolve(sessionId).resolve("history.log");
        if (!Files.exists(historyPath)) return "No history found";
        return Files.readString(historyPath);
    }

    @Tool(name = "sessions_history_add", description = "Add to session history")
    public String sessionsHistoryAdd(String sessionId, String message) throws IOException {
        Path historyPath = sessionDir.resolve(sessionId).resolve("history.log");
        if (!Files.exists(historyPath)) return "Session not found";
        
        String entry = String.format("[%s] %s\n", Instant.now().toString(), message);
        Files.writeString(historyPath, entry, StandardOpenOption.APPEND);
        return "Added to history";
    }

    @Tool(name = "sessions_history_clear", description = "Clear session history")
    public String sessionsHistoryClear(String sessionId) throws IOException {
        Path historyPath = sessionDir.resolve(sessionId).resolve("history.log");
        if (!Files.exists(historyPath)) return "Session not found";
        Files.writeString(historyPath, "");
        return "History cleared";
    }

    @Tool(name = "sessions_send", description = "Send message to session")
    public String sessionsSend(String sessionId, String message) throws IOException {
        Path sessionPath = sessionDir.resolve(sessionId);
        if (!Files.exists(sessionPath)) return "Session not found";
        
        String entry = String.format("[%s] SENT: %s\n", Instant.now().toString(), message);
        Files.writeString(sessionPath.resolve("history.log"), entry, StandardOpenOption.APPEND);
        
        Path inboxPath = sessionPath.resolve("inbox");
        Files.createDirectories(inboxPath);
        String msgFile = "msg_" + System.currentTimeMillis() + ".txt";
        Files.writeString(inboxPath.resolve(msgFile), message);
        
        return "Message sent to session: " + sessionId;
    }

    @Tool(name = "sessions_spawn", description = "Spawn child session")
    public String sessionsSpawn(String parentSessionId, String name) throws IOException {
        String childId = UUID.randomUUID().toString();
        Path childPath = sessionDir.resolve(childId);
        Files.createDirectories(childPath);
        
        String meta = String.format(
            "{\"name\":\"%s\",\"parent\":\"%s\",\"created\":\"%s\",\"status\":\"active\"}", 
            name, parentSessionId, Instant.now().toString());
        Files.writeString(childPath.resolve("meta.json"), meta);
        Files.createFile(childPath.resolve("history.log"));
        
        return "Spawned child session: " + childId;
    }

    @Tool(name = "sessions_yield", description = "Yield to another session")
    public String sessionsYield(String fromSessionId, String toSessionId) throws IOException {
        updateStatus(fromSessionId, "yielded");
        updateStatus(toSessionId, "active");
        return "Yielded from " + fromSessionId + " to " + toSessionId;
    }

    @Tool(name = "subagents", description = "List child sessions")
    public String subagents(String parentSessionId) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.list(sessionDir)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                try {
                    String meta = Files.readString(dir.resolve("meta.json"));
                    if (meta.contains("\"parent\":\"" + parentSessionId + "\"")) {
                        sb.append(dir.getFileName()).append("\n");
                    }
                } catch (Exception e) {}
            });
        return sb.length() > 0 ? sb.toString() : "No child sessions";
    }

    @Tool(name = "sessions_count", description = "Count sessions")
    public int sessionsCount() throws IOException {
        return (int) Files.list(sessionDir).filter(Files::isDirectory).count();
    }

    private String updateStatus(String sessionId, String status) throws IOException {
        Path metaPath = sessionDir.resolve(sessionId).resolve("meta.json");
        if (!Files.exists(metaPath)) return "Session not found";
        
        String meta = Files.readString(metaPath);
        meta = meta.replaceAll("\"status\":\"[^\"]+\"", "\"status\":\"" + status + "\"");
        Files.writeString(metaPath, meta);
        return "Status updated to: " + status;
    }

    private void loadSessions() throws IOException {
        Files.list(sessionDir)
            .filter(Files::isDirectory)
            .forEach(dir -> {
                try {
                    String id = dir.getFileName().toString();
                    String meta = Files.readString(dir.resolve("meta.json"));
                    String name = meta.replaceAll(".*\"name\":\"([^\"]+)\".*", "$1");
                    activeSessions.put(id, new Session(id, name));
                } catch (Exception e) {}
            });
    }

    private static class Session {
        final String id, name;
        Session(String id, String name) { this.id = id; this.name = name; }
    }
}
