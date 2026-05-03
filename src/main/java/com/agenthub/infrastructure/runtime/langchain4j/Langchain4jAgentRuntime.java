package com.agenthub.infrastructure.runtime.langchain4j;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import com.agenthub.domain.model.Agent;
import com.agenthub.domain.port.runtime.AgentRuntime;
import com.agenthub.domain.port.runtime.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class Langchain4jAgentRuntime implements AgentRuntime {
    
    private static final String TYPE = "LANGCHAIN4J";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, SupervisorAgent> supervisors = new ConcurrentHashMap<>();
    
    @Override
    public String initialize(Agent agent, Map<String, Object> config) {
        String id = generateId(agent.getId());
        pool.submit(() -> doInit(id, agent));
        waitInit(id);
        return id;
    }
    
    private void doInit(String id, Agent agent) {
        status.put(id, RuntimeStatus.INITIALIZED);
        agents.put(id, agent);
        supervisors.put(id, createSupervisor(agent));
    }
    
    private SupervisorAgent createSupervisor(Agent agent) {
        return AgenticServices.supervisorBuilder().build();
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
        SupervisorAgent supervisor = supervisors.get(id);
        return supervisor.invoke(msg);
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
        supervisors.remove(id);
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
        return "langchain4j-" + agentId + "-" + System.currentTimeMillis();
    }
    
    private void waitInit(String id) {
        for (int i = 0; i < 50 && status.get(id) == null; i++) {
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }
}
