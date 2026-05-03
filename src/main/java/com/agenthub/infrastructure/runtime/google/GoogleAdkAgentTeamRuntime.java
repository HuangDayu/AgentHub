package com.agenthub.infrastructure.runtime.google;

import com.agenthub.domain.model.AgentTeam;
import com.agenthub.domain.port.runtime.AgentRuntime;
import com.agenthub.domain.port.runtime.AgentTeamRuntime;
import com.agenthub.domain.port.runtime.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GoogleAdkAgentTeamRuntime implements AgentTeamRuntime {
    
    private static final String TYPE = "GOOGLE_ADK";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, AgentTeam> teams = new ConcurrentHashMap<>();
    private final Map<String, ThreadGroup> groups = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();
    
    @Override
    public String initialize(AgentTeam team, Map<String, Object> config) {
        String id = generateId(team.getId());
        pool.submit(() -> doInit(id, team));
        return id;
    }
    
    private void doInit(String id, AgentTeam team) {
        status.put(id, RuntimeStatus.INITIALIZED);
        teams.put(id, team);
        groups.put(id, new ThreadGroup("team-" + id));
        sessions.put(id, new ConcurrentHashMap<>());
    }
    
    @Override
    public void startSession(String id, String sid, Map<String, Object> ctx) {
        validate(id);
        pool.submit(() -> doStart(id, sid, ctx));
    }
    
    private void doStart(String id, String sid, Map<String, Object> ctx) {
        status.put(id, RuntimeStatus.RUNNING);
        sessions.get(id).put(sid, ctx);
    }
    
    @Override
    public CompletableFuture<String> sendMessage(String id, String sid, String msg) {
        validate(id);
        return CompletableFuture.supplyAsync(() -> doSend(id, msg), pool);
    }
    
    private String doSend(String id, String msg) {
        AgentTeam team = teams.get(id);
        return String.format("[Team - %s] %s", team.getName(), msg);
    }
    
    @Override
    public void streamMessage(String id, String sid, String msg, AgentRuntime.StreamCallback cb) {
        validate(id);
        pool.submit(() -> doStream(id, msg, cb));
    }
    
    private void doStream(String id, String msg, AgentRuntime.StreamCallback cb) {
        try {
            String resp = doSend(id, msg);
            for (String token : resp.split(" ")) {
                cb.onToken(token + " ");
                Thread.sleep(50);
            }
            cb.onComplete();
        } catch (Exception e) {
            cb.onError(e);
        }
    }
    
    @Override
    public void endSession(String id, String sid) {
        validate(id);
        pool.submit(() -> doEnd(id, sid));
    }
    
    private void doEnd(String id, String sid) {
        sessions.get(id).remove(sid);
        status.put(id, RuntimeStatus.STOPPED);
    }
    
    @Override
    public void destroy(String id) {
        validate(id);
        pool.submit(() -> doDestroy(id));
    }
    
    private void doDestroy(String id) {
        teams.remove(id);
        sessions.remove(id);
        ThreadGroup g = groups.remove(id);
        if (g != null) g.interrupt();
        status.put(id, RuntimeStatus.DESTROYED);
    }
    
    @Override
    public RuntimeStatus getStatus(String id) {
        return status.getOrDefault(id, RuntimeStatus.DESTROYED);
    }
    
    @Override
    public String getFrameworkType() {
        return TYPE;
    }
    
    private void validate(String id) {
        if (!teams.containsKey(id)) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
    }
    
    private String generateId(String teamId) {
        return "google-adk-team-" + teamId + "-" + System.currentTimeMillis();
    }
}
