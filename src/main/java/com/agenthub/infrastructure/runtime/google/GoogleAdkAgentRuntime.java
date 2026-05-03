package com.agenthub.infrastructure.runtime.google;

import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import com.agenthub.domain.model.Agent;
import com.agenthub.application.port.out.AgentRuntime;
import com.agenthub.application.port.out.RuntimeStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GoogleAdkAgentRuntime implements AgentRuntime {
    
    private static final String TYPE = "GOOGLE_ADK";
    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RuntimeStatus> status = new ConcurrentHashMap<>();
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, Runner> runners = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIds = new ConcurrentHashMap<>();
    
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
        LlmAgent adkAgent = createLlmAgent(agent, config);
        runners.put(id, createRunner(adkAgent));
        sessionIds.put(id, "session-" + System.currentTimeMillis());
    }
    
    private LlmAgent createLlmAgent(Agent agent, Map<String, Object> config) {
        return LlmAgent.builder()
            .name(agent.getAgentCode())
            .description(agent.getDescription() != null ? agent.getDescription() : "")
            .model((String) config.getOrDefault("model", "gemini-2.0-flash"))
            .instruction((String) config.getOrDefault("instruction", "You are a helpful assistant."))
            .build();
    }
    
    private Runner createRunner(LlmAgent agent) {
        return new Runner(agent, "agenthub", null, new InMemorySessionService());
    }
    
    @Override
    public void startSession(String id, String sid, Map<String, Object> ctx) {
        validate(id);
        pool.submit(() -> doStart(id, sid));
    }
    
    private void doStart(String id, String sid) {
        status.put(id, RuntimeStatus.RUNNING);
        sessionIds.put(id, sid);
    }
    
    @Override
    public CompletableFuture<String> sendMessage(String id, String sid, String msg) {
        validate(id);
        return CompletableFuture.supplyAsync(() -> doSend(id, sid, msg), pool);
    }
    
    private String doSend(String id, String sid, String msg) {
        Runner runner = runners.get(id);
        Content userMsg = Content.fromParts(Part.fromText(msg));
        Flowable<Event> flow = runner.runAsync("user", sid, userMsg);
        StringBuilder result = new StringBuilder();
        flow.blockingForEach(event -> appendEventText(event, result));
        return result.toString();
    }
    
    private void appendEventText(Event event, StringBuilder result) {
        event.content().ifPresent(content -> {
            content.parts().ifPresent(parts -> {
                parts.forEach(part -> part.text().ifPresent(result::append));
            });
        });
    }
    
    @Override
    public void streamMessage(String id, String sid, String msg, StreamCallback cb) {
        validate(id);
        pool.submit(() -> doStream(id, sid, msg, cb));
    }
    
    private void doStream(String id, String sid, String msg, StreamCallback cb) {
        try {
            Runner runner = runners.get(id);
            Content userMsg = Content.fromParts(Part.fromText(msg));
            Flowable<Event> flow = runner.runAsync("user", sid, userMsg);
            flow.blockingForEach(event -> processStreamEvent(event, cb));
            cb.onComplete();
        } catch (Exception e) {
            cb.onError(e);
        }
    }
    
    private void processStreamEvent(Event event, StreamCallback cb) {
        event.content().ifPresent(content -> {
            content.parts().ifPresent(parts -> {
                parts.forEach(part -> part.text().ifPresent(cb::onToken));
            });
        });
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
        runners.remove(id);
        agents.remove(id);
        sessionIds.remove(id);
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
        return "google-adk-" + agentId + "-" + System.currentTimeMillis();
    }
    
    private void waitInit(String id) {
        for (int i = 0; i < 50 && status.get(id) == null; i++) {
            try { Thread.sleep(100); } catch (InterruptedException e) { break; }
        }
    }
}
