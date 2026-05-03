package com.agenthub.infrastructure.runtime.openjiuwen;

import com.openjiuwen.core.singleagent.agents.ReActAgent;
import com.openjiuwen.core.singleagent.schema.AgentCard;
import com.openjiuwen.core.session.AgentSessionApi;
import com.agenthub.domain.model.Agent;
import com.agenthub.application.port.out.AgentRuntime;
import com.agenthub.application.port.out.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;
import java.util.List;
import java.util.Iterator;

@Component
public class OpenJiuwenAgentRuntime implements AgentRuntime {
    
    private static final String TYPE = "OPENJIUWEN";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, ReActAgent> reactAgents = new ConcurrentHashMap<>();
    private final Map<String, AgentSessionApi> sessions = new ConcurrentHashMap<>();
    
    @Override
    public String initialize(Agent agent, Map<String, Object> config) {
        String id = generateId(agent.getId());
        pool.submit(() -> doInit(id, agent, config));
        waitInit(id);
        return id;
    }
    
    private void doInit(String id, Agent agent, Map<String, Object> config) {
        status.put(id, RuntimeStatus.INITIALIZED);
        agents.put(id, agent);
        reactAgents.put(id, createReActAgent(agent, config));
    }
    
    private ReActAgent createReActAgent(Agent agent, Map<String, Object> config) {
        AgentCard card = new AgentCard();
        card.setName(agent.getName());
        card.setDescription(agent.getDescription());
        return new ReActAgent(card);
    }
    
    @Override
    public void startSession(String id, String sid, Map<String, Object> ctx) {
        validate(id);
        pool.submit(() -> doStart(id, sid));
    }
    
    private void doStart(String id, String sid) {
        status.put(id, RuntimeStatus.RUNNING);
        sessions.put(id, new AgentSessionApi(sid));
    }
    
    @Override
    public CompletableFuture<String> sendMessage(String id, String sid, String msg) {
        validate(id);
        return CompletableFuture.supplyAsync(() -> doSend(id, msg), pool);
    }
    
    private String doSend(String id, String msg) {
        ReActAgent agent = reactAgents.get(id);
        AgentSessionApi session = sessions.get(id);
        Object result = agent.invoke(msg, session);
        return result.toString();
    }
    
    @Override
    public void streamMessage(String id, String sid, String msg, StreamCallback cb) {
        validate(id);
        pool.submit(() -> doStream(id, msg, cb));
    }
    
    private void doStream(String id, String msg, StreamCallback cb) {
        try {
            ReActAgent agent = reactAgents.get(id);
            AgentSessionApi session = sessions.get(id);
            Iterator<Object> stream = agent.stream(msg, session, List.of());
            while (stream.hasNext()) {
                cb.onToken(stream.next().toString());
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
        sessions.remove(id);
        status.put(id, RuntimeStatus.STOPPED);
    }
    
    @Override
    public void destroy(String id) {
        validate(id);
        pool.submit(() -> doDestroy(id));
    }
    
    private void doDestroy(String id) {
        reactAgents.remove(id);
        sessions.remove(id);
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
        return "openjiuwen-" + agentId + "-" + System.currentTimeMillis();
    }
    
    private void waitInit(String id) {
        for (int i = 0; i < 50 && status.get(id) == null; i++) {
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }
}
