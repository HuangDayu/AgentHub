package com.agenthub.test.integration;

import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.application.port.out.repositories.SpanRepository;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.trace.Span;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import com.agenthub.test.TestAgentHubApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.agenthub.test.common.TestCommonTools.getRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestAgentHubApplication.class)
class RuntimeDataViewControllerIntegrationTest {
    private static final String WORKSPACE_ID = "100000002";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SpanRepository spanRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(getRequestBuilder()).build();
    }

    @AfterEach
    void cleanup() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldReturnRuntimeDataView() throws Exception {
        String agentId = createAgent();
        Session session = withTenant(() -> sessionRepository.save(session(agentId)));
        withTenant(() -> spanRepository.save(span(agentId, session.getId())));
        expectDataView(agentId, session.getId());
    }

    @Test
    void shouldNotLeakSpanWhenSessionNotOwnedByAgent() throws Exception {
        String ownerAgentId = createAgent();
        String otherAgentId = createAgent();
        Session session = withTenant(() -> sessionRepository.save(session(ownerAgentId)));
        withTenant(() -> spanRepository.save(span(ownerAgentId, session.getId())));
        expectIsolatedDataView(otherAgentId, session.getId());
    }

    @Test
    void shouldSortRunsAndSpansByTimeDesc() throws Exception {
        String agentId = createAgent();
        Session older = withTenant(() -> sessionRepository.save(namedSession(agentId, "Older", Instant.parse("2026-01-01T00:00:00Z"))));
        Session newer = withTenant(() -> sessionRepository.save(namedSession(agentId, "Newer", Instant.parse("2026-01-02T00:00:00Z"))));
        withTenant(() -> saveSpans(agentId, newer.getId()));
        expectSortedDataView(agentId, newer.getId(), newer.getId(), "span-new");
    }

    @Test
    void shouldReturnTraceProblemSummary() throws Exception {
        String agentId = createAgent();
        Session session = withTenant(() -> sessionRepository.save(session(agentId)));
        withTenant(() -> saveProblemSpans(agentId, session.getId()));
        expectProblemSummary(agentId, session.getId());
    }

    @Test
    void shouldReturnEmptyViewWhenSessionNotOwnedByAgent() throws Exception {
        String ownerAgentId = createAgent();
        String otherAgentId = createAgent();
        Session session = withTenant(() -> sessionRepository.save(session(ownerAgentId)));
        withTenant(() -> mockMvc.perform(get(path(otherAgentId, session.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedRun.id").value(session.getId()))
                .andExpect(jsonPath("$.trace.spanCount").value(0)));
    }

    private void expectDataView(String agentId, String sessionId) throws Exception {
        withTenant(() -> mockMvc.perform(get(path(agentId, sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedRun.id").value(sessionId))
                .andExpect(jsonPath("$.trace.spanCount").value(1))
                .andExpect(jsonPath("$.modelInvocationData.chat.totalTokens.totalTokens").value(15.0)));
    }

    private void expectIsolatedDataView(String agentId, String sessionId) throws Exception {
        withTenant(() -> mockMvc.perform(get(path(agentId, sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trace.spanCount").value(0))
                .andExpect(jsonPath("$.spanTree.length()").value(0)));
    }

    private void expectSortedDataView(String agentId, String sessionId, String runId, String spanId) throws Exception {
        withTenant(() -> mockMvc.perform(get(path(agentId, sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedRun.id").value(runId))
                .andExpect(jsonPath("$.spanTree[0].spanId").value(spanId)));
    }

    private void expectProblemSummary(String agentId, String sessionId) throws Exception {
        withTenant(() -> mockMvc.perform(get(path(agentId, sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trace.errorSpanCount").value(1))
                .andExpect(jsonPath("$.trace.slowestSpanId").value("slow-span"))
                .andExpect(jsonPath("$.trace.slowestSpanName").value("slow"))
                .andExpect(jsonPath("$.trace.slowestLatencyNs").value(9000))
                .andExpect(jsonPath("$.errorSpans[0].spanId").value("error-span"))
                .andExpect(jsonPath("$.slowSpans[0].spanId").value("slow-span")));
    }

    private String createAgent() throws Exception {
        return withTenant(() -> createAgentWithTenant());
    }

    private String createAgentWithTenant() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String json = mockMvc.perform(post("/api/v1/workspaces/{workspaceId}/agents", WORKSPACE_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(agentJson(suffix)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return id(json);
    }

    private String agentJson(String suffix) {
        return "{\"agentCode\":\"runtime-agent-" + suffix + "\",\"name\":\"Runtime Agent " + suffix + "\",\"description\":\"test\"}";
    }

    private String id(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asText();
    }

    private Session session(String agentId) {
        return new Session(null, agentId, "Runtime Session", null, null, Instant.now());
    }

    private Session namedSession(String agentId, String name, Instant createdAt) {
        return new Session(null, agentId, name, null, null, createdAt);
    }

    private Span span(String agentId, String runId) {
        Span span = Span.create("trace-runtime", "span-runtime", "chat");
        fillSpan(span, agentId, runId);
        return span;
    }

    private Void saveSpans(String agentId, String runId) {
        spanRepository.save(timedSpan(agentId, runId, "span-old", "100"));
        spanRepository.save(timedSpan(agentId, runId, "span-new", "200"));
        return null;
    }

    private Void saveProblemSpans(String agentId, String runId) {
        spanRepository.save(problemSpan(new ProblemSpec(agentId, runId, "error-span", "error", 3000L, 2)));
        spanRepository.save(problemSpan(new ProblemSpec(agentId, runId, "slow-span", "slow", 9000L, 0)));
        return null;
    }

    private Span problemSpan(ProblemSpec spec) {
        Span span = timedSpan(spec.agentId, spec.runId, spec.spanId, "300");
        span.setName(spec.name);
        span.setLatencyNs(spec.latency);
        span.setStatusCode(spec.statusCode);
        return span;
    }

    private static final class ProblemSpec {
        private final String agentId;
        private final String runId;
        private final String spanId;
        private final String name;
        private final Long latency;
        private final Integer statusCode;

        ProblemSpec(String agentId, String runId, String spanId, String name, Long latency, Integer statusCode) {
            this.agentId = agentId;
            this.runId = runId;
            this.spanId = spanId;
            this.name = name;
            this.latency = latency;
            this.statusCode = statusCode;
        }
    }

    private Span timedSpan(String agentId, String runId, String spanId, String start) {
        Span span = Span.create("trace-runtime", spanId, "chat");
        fillSpan(span, agentId, runId);
        span.setStartTimeUnixNano(start);
        span.setEndTimeUnixNano(String.valueOf(Long.parseLong(start) + 10));
        return span;
    }

    private void fillSpan(Span span, String agentId, String runId) {
        span.setRunId(runId);
        span.setAgentId(agentId);
        span.setModel("qwen-plus");
        span.setInputTokens(10L);
        span.setOutputTokens(5L);
        span.setTotalTokens(15L);
        span.setLatencyNs(1000L);
        span.setStartTimeUnixNano("100");
        span.setEndTimeUnixNano("1100");
        span.setAttributes(Map.of("gen_ai.request.model", "qwen-plus"));
    }

    private String path(String agentId, String sessionId) {
        return "/api/v1/workspaces/" + WORKSPACE_ID + "/agents/" + agentId + "/sessions/" + sessionId + "/data-view";
    }

    private <T> T withTenant(TenantAction<T> action) throws Exception {
        try (var ignored = TenantContextHolder.open(testContext())) {
            return action.run();
        }
    }

    private TenantThreadContext testContext() {
        return new TenantThreadContext(WORKSPACE_ID, WORKSPACE_ID, "runtime-data-view-test", "1", "1", "test-user", false);
    }

    @FunctionalInterface
    private interface TenantAction<T> {
        T run() throws Exception;
    }
}
