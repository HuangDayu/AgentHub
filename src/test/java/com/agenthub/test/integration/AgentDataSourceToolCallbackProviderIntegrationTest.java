package com.agenthub.test.integration;

import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.AgentDataSourceStatus;
import com.agenthub.domain.event.AgentDataSourceChangedEvent;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.context.TenantContextHolder;
import com.agenthub.infrastructure.context.TenantThreadContext;
import com.agenthub.infrastructure.tools.data_source.AgentDataSourceToolCallbackProvider;
import com.agenthub.infrastructure.tools.data_source.AgentDataSourceToolFactory;
import com.agenthub.test.TestAgentHubApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Agent 数据源 ToolCallback 集成测试 - 验证同进程路径的事件驱动刷新。
 */
@SpringBootTest(classes = TestAgentHubApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AgentDataSourceToolCallbackProviderIntegrationTest {

    @Autowired
    private AgentDataSourceRepository repository;

    @Autowired
    private AgentDataSourceToolCallbackProvider provider;

    @Autowired
    private AgentDataSourceToolFactory factory;

    @Autowired
    private WebApplicationContext context;

    private static String enabledSourceId;
    private static String disabledSourceId;

    @AfterAll
    void cleanup() {
        if (enabledSourceId != null) safeDelete(enabledSourceId);
        if (disabledSourceId != null) safeDelete(disabledSourceId);
    }

    private void safeDelete(String id) {
        try (var ignored = withTenant()) {
            repository.deleteById(id);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @Order(1)
    void shouldStartWithNonNullProvider() {
        assertNotNull(provider);
        assertNotNull(provider.getToolCallbacks());
    }

    @Test
    @Order(2)
    void shouldRefreshOnCreatedEvent() {
        try (var ignored = withTenant()) {
            String unique = UUID.randomUUID().toString().substring(0, 8);
            AgentDataSource enabled = newDataSource("ds-tool-enabled-" + unique, true);
            AgentDataSource disabled = newDataSource("ds-tool-disabled-" + unique, false);
            enabledSourceId = repository.save(enabled).getId();
            disabledSourceId = repository.save(disabled).getId();
        }
        publishEvent(enabledSourceId, AgentDataSourceChangedEvent.ChangeType.CREATED);
        publishEvent(disabledSourceId, AgentDataSourceChangedEvent.ChangeType.CREATED);

        List<String> names = namesOf(provider.getToolCallbacks());
        assertTrue(names.stream().anyMatch(n -> n.contains(enabledSourceId.replace("-", "_"))),
            "enabled tool should appear after refresh");
        assertTrue(names.stream().noneMatch(n -> n.contains(disabledSourceId.replace("-", "_"))),
            "disabled tool must not appear");
    }

    @Test
    @Order(3)
    void shouldRemoveOnDisabledEvent() {
        try (var ignored = withTenant()) {
            AgentDataSource source = repository.findById(enabledSourceId).orElseThrow();
            source.setEnabled(false);
            source.setStatus(AgentDataSourceStatus.DISABLED);
            repository.save(source);
        }
        publishEvent(enabledSourceId, AgentDataSourceChangedEvent.ChangeType.DISABLED);
        List<String> names = namesOf(provider.getToolCallbacks());
        assertTrue(names.stream().noneMatch(n -> n.contains(enabledSourceId.replace("-", "_"))),
            "tool must be removed after disable event");
    }

    @Test
    @Order(4)
    void shouldAddBackOnEnabledEvent() {
        try (var ignored = withTenant()) {
            AgentDataSource source = repository.findById(enabledSourceId).orElseThrow();
            source.setEnabled(true);
            source.setStatus(AgentDataSourceStatus.ENABLED);
            repository.save(source);
        }
        publishEvent(enabledSourceId, AgentDataSourceChangedEvent.ChangeType.ENABLED);
        List<String> names = namesOf(provider.getToolCallbacks());
        assertTrue(names.stream().anyMatch(n -> n.contains(enabledSourceId.replace("-", "_"))),
            "tool must be re-added after enable event");
    }

    @Test
    @Order(5)
    void shouldRemoveOnDeletedEvent() {
        try (var ignored = withTenant()) {
            repository.deleteById(enabledSourceId);
        }
        publishEvent(enabledSourceId, AgentDataSourceChangedEvent.ChangeType.DELETED);
        List<String> names = namesOf(provider.getToolCallbacks());
        assertTrue(names.stream().noneMatch(n -> n.contains(enabledSourceId.replace("-", "_"))),
            "tool must be removed after delete event");
        enabledSourceId = null;
    }

    @Test
    @Order(6)
    void shouldExposeSingleCallbackPerEnabledSource() {
        try (var ignored = withTenant()) {
            AgentDataSource enabled = newDataSource("ds-tool-multi-" + UUID.randomUUID().toString().substring(0, 8), true);
            String id = repository.save(enabled).getId();
            publishEvent(id, AgentDataSourceChangedEvent.ChangeType.ENABLED);
            publishEvent(id, AgentDataSourceChangedEvent.ChangeType.ENABLED);
            long matches = namesOf(provider.getToolCallbacks()).stream()
                .filter(n -> n.contains(id.replace("-", "_")))
                .count();
            assertEquals(1, matches, "no duplicate tool callback for same source");
            repository.deleteById(id);
        }
    }

    @Test
    @Order(7)
    void shouldBuildToolCallbackWithExpectedMetadata() {
        AgentDataSource source = newDataSource("ds-tool-meta-" + UUID.randomUUID().toString().substring(0, 8), true);
        ToolCallback callback = factory.toToolCallback(source);
        assertNotNull(callback);
        assertNotNull(callback.getToolDefinition());
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("agent_data_source_invoke_"), "tool name prefix: " + name);
        assertTrue(callback.getToolDefinition().description().contains(source.getName()),
            "description should mention data source name");
    }

    private void publishEvent(String id, AgentDataSourceChangedEvent.ChangeType type) {
        ApplicationEventPublisher publisher = context;
        publisher.publishEvent(new AgentDataSourceChangedEvent(id, "ws-tool-test", type));
    }

    private static List<String> namesOf(ToolCallback[] callbacks) {
        return java.util.Arrays.stream(callbacks)
            .map(c -> c.getToolDefinition().name())
            .toList();
    }

    private static TenantContextHolder.TenantContextScope withTenant() {
        return TenantContextHolder.open(TenantThreadContext.builder()
            .tenantId("100000002")
            .workspaceId("ws-tool-test")
            .requestId(UUID.randomUUID().toString())
            .build());
    }

    private static AgentDataSource newDataSource(String name, boolean enabled) {
        AgentDataSource s = new AgentDataSource();
        s.setId(UUID.randomUUID().toString());
        s.setTenantId("100000002");
        s.setWorkspaceId("ws-tool-test");
        s.setName(name);
        s.setDescription("test data source");
        s.setProtocol(AgentDataSourceProtocol.JDBC);
        s.setEndpointUri("jdbc:postgresql://localhost:5432/test?dummy=1");
        s.setPropertiesJson("{}");
        s.setEnabled(enabled);
        s.setStatus(enabled ? AgentDataSourceStatus.ENABLED : AgentDataSourceStatus.DISABLED);
        s.setCreatedAt(java.time.Instant.now());
        s.setUpdatedAt(java.time.Instant.now());
        return s;
    }
}
