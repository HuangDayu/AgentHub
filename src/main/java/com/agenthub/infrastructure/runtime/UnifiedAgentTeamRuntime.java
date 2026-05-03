package com.agenthub.infrastructure.runtime;

import com.agenthub.domain.model.AgentTeam;
import com.agenthub.application.port.out.AgentRuntime;
import com.agenthub.application.port.out.AgentTeamRuntime;
import com.agenthub.application.port.out.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;
import java.util.List;

@Component
public class UnifiedAgentTeamRuntime implements AgentTeamRuntime {
    
    private static final String TYPE = "UNIFIED";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, AgentTeam> teams = new ConcurrentHashMap<>();
    private final Map<String, ThreadGroup> groups = new ConcurrentHashMap<>();
    private final Map<String, List<AgentRuntime>> runtimes = new ConcurrentHashMap<>();
    
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
    }
    
    @Override
    public void startSession(String id, String sid, Map<String, Object> ctx) {
        validate(id);
        pool.submit(() -> doStart(id, sid, ctx));
    }
    
    private void doStart(String id, String sid, Map<String, Object> ctx) {
        status.put(id, RuntimeStatus.RUNNING);
        List<AgentRuntime> teamRuntimes = runtimes.get(id);
        if (teamRuntimes != null) {
            teamRuntimes.forEach(rt -> rt.startSession(id, sid, ctx));
        }
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
            String result = doSend(id, msg);
            cb.onToken(result);
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
        List<AgentRuntime> teamRuntimes = runtimes.get(id);
        if (teamRuntimes != null) {
            teamRuntimes.forEach(rt -> rt.endSession(id, sid));
        }
        status.put(id, RuntimeStatus.STOPPED);
    }
    
    @Override
    public void destroy(String id) {
        validate(id);
        pool.submit(() -> doDestroy(id));
    }
    
    private void doDestroy(String id) {
        teams.remove(id);
        runtimes.remove(id);
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
        return "team-" + teamId + "-" + System.currentTimeMillis();
    }
}
