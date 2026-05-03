package com.agenthub.infrastructure.runtime.embabel;

import com.embabel.agent.core.Agent;
import com.embabel.agent.api.common.autonomy.Autonomy;
import com.embabel.agent.api.common.autonomy.AgentProcessExecution;
import com.embabel.agent.core.ProcessOptions;
import com.agenthub.application.port.out.AgentRuntime;
import com.agenthub.application.port.out.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class EmbabelAgentRuntime implements AgentRuntime {
    
    private static final String TYPE = "EMBABEL";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, com.agenthub.domain.model.Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, Autonomy> autonomies = new ConcurrentHashMap<>();
    
    @Override
    public String initialize(com.agenthub.domain.model.Agent agent, Map<String, Object> config) {
        String id = generateId(agent.getId());
        pool.submit(() -> doInit(id, agent, config));
        waitInit(id);
        return id;
    }
    
    private void doInit(String id, com.agenthub.domain.model.Agent agent, Map<String, Object> config) {
        status.put(id, RuntimeStatus.INITIALIZED);
        agents.put(id, agent);
        autonomies.put(id, createAutonomy(config));
    }
    
    private Autonomy createAutonomy(Map<String, Object> config) {
        return new Autonomy(null, null, null);
    }
    
    @Override
    public void startSession(String id, String sid, Map<String, Object> ctx) {
        validate(id);
        pool.submit(() -> doStart(id, sid));
    }
    
    private void doStart(String id, String sid) {
        status.put(id, RuntimeStatus.RUNNING);
    }
    
    @Override
    public CompletableFuture<String> sendMessage(String id, String sid, String msg) {
        validate(id);
        return CompletableFuture.supplyAsync(() -> doSend(id, msg), pool);
    }
    
    private String doSend(String id, String msg) {
        Autonomy autonomy = autonomies.get(id);
        com.agenthub.domain.model.Agent agent = agents.get(id);
        Agent embabelAgent = createEmbabelAgent(agent);
        AgentProcessExecution result = autonomy.runAgent(msg, new ProcessOptions(), embabelAgent);
        return result.getOutput().toString();
    }
    
    private Agent createEmbabelAgent(com.agenthub.domain.model.Agent agent) {
        return new Agent(agent.getName(), "agenthub", "1.0", agent.getDescription(), 
            java.util.Set.of(), java.util.List.of(), java.util.Set.of(), null);
    }
    
    @Override
    public void streamMessage(String id, String sid, String msg, StreamCallback cb) {
        validate(id);
        pool.submit(() -> doStream(id, msg, cb));
    }
    
    private void doStream(String id, String msg, StreamCallback cb) {
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
        status.put(id, RuntimeStatus.STOPPED);
    }
    
    @Override
    public void destroy(String id) {
        validate(id);
        pool.submit(() -> doDestroy(id));
    }
    
    private void doDestroy(String id) {
        autonomies.remove(id);
        agents.remove(id);
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
        if (!agents.containsKey(id)) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
    }
    
    private String generateId(String agentId) {
        return "embabel-" + agentId + "-" + System.currentTimeMillis();
    }
    
    private void waitInit(String id) {
        for (int i = 0; i < 50 && status.get(id) == null; i++) {
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }
}
