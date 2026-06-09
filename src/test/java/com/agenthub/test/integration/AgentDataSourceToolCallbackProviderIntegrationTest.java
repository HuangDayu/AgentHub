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
    private static String enabledToolPrefix;
    private static String disabledSourceId;
    private static String disabledToolPrefix;

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
            enabledToolPrefix = sanitize(enabled.getName());
            disabledSourceId = repository.save(disabled).getId();
            disabledToolPrefix = sanitize(disabled.getName());
        }
        publishEvent(enabledSourceId, AgentDataSourceChangedEvent.ChangeType.CREATED);
        publishEvent(disabledSourceId, AgentDataSourceChangedEvent.ChangeType.CREATED);

        List<String> names = namesOf(provider.getToolCallbacks());
        assertTrue(names.stream().anyMatch(n -> n.contains(enabledToolPrefix)),
            "enabled tool should appear after refresh");
        assertTrue(names.stream().noneMatch(n -> n.contains(disabledToolPrefix)),
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
        assertTrue(names.stream().noneMatch(n -> n.contains(enabledToolPrefix)),
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
        assertTrue(names.stream().anyMatch(n -> n.contains(enabledToolPrefix)),
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
        assertTrue(names.stream().noneMatch(n -> n.contains(enabledToolPrefix)),
            "tool must be removed after delete event");
        enabledSourceId = null;
    }

    @Test
    @Order(6)
    void shouldExposeSingleCallbackPerEnabledSource() {
        try (var ignored = withTenant()) {
            String unique = UUID.randomUUID().toString().substring(0, 8);
            AgentDataSource enabled = newDataSource("ds-tool-multi-" + unique, true);
            String id = repository.save(enabled).getId();
            String toolPrefix = sanitize(enabled.getName());
            publishEvent(id, AgentDataSourceChangedEvent.ChangeType.ENABLED);
            publishEvent(id, AgentDataSourceChangedEvent.ChangeType.ENABLED);
            long matches = namesOf(provider.getToolCallbacks()).stream()
                .filter(n -> n.contains(toolPrefix))
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
        assertTrue(name.startsWith("sql_query_"), "JDBC tool name should start with sql_query_: " + name);
        assertTrue(callback.getToolDefinition().description().contains(source.getName()),
            "description should mention data source name");
    }

    @Test
    @Order(8)
    void shouldGenerateJdbcSchemaWithSqlField() {
        AgentDataSource source = newDataSource("ds-tool-jdbc-" + UUID.randomUUID().toString().substring(0, 8), true);
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"sql\""), "JDBC schema should have 'sql' field: " + schema);
        assertTrue(schema.contains("SELECT"), "JDBC schema should mention SELECT: " + schema);
    }

    @Test
    @Order(9)
    void shouldGenerateHttpSchemaWithMethodAndPath() {
        AgentDataSource source = newHttpSource("ds-tool-http-" + UUID.randomUUID().toString().substring(0, 8));
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"method\""), "HTTP schema should have 'method': " + schema);
        assertTrue(schema.contains("\"path\""), "HTTP schema should have 'path': " + schema);
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("http_call_"), "HTTP tool name should start with http_call_: " + name);
    }

    @Test
    @Order(10)
    void shouldGenerateMongoDbSchemaWithCollectionAndOperation() {
        AgentDataSource source = newMongoSource("ds-tool-mongo-" + UUID.randomUUID().toString().substring(0, 8));
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"collection\""), "MongoDB schema should have 'collection': " + schema);
        assertTrue(schema.contains("\"operation\""), "MongoDB schema should have 'operation': " + schema);
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("mongo_op_"), "MongoDB tool name should start with mongo_op_: " + name);
    }

    @Test
    @Order(11)
    void shouldGenerateRedisSchemaWithCommandAndKey() {
        AgentDataSource source = newRedisSource("ds-tool-redis-" + UUID.randomUUID().toString().substring(0, 8));
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"command\""), "Redis schema should have 'command': " + schema);
        assertTrue(schema.contains("\"key\""), "Redis schema should have 'key': " + schema);
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("redis_cmd_"), "Redis tool name should start with redis_cmd_: " + name);
    }

    @Test
    @Order(12)
    void shouldGenerateKafkaSchemaWithMessage() {
        AgentDataSource source = newKafkaSource("ds-tool-kafka-" + UUID.randomUUID().toString().substring(0, 8));
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"message\""), "Kafka schema should have 'message': " + schema);
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("kafka_send_"), "Kafka tool name should start with kafka_send_: " + name);
    }

    @Test
    @Order(13)
    void shouldGenerateFtpSchemaWithOperationAndPath() {
        AgentDataSource source = newFtpSource("ds-tool-ftp-" + UUID.randomUUID().toString().substring(0, 8));
        ToolCallback callback = factory.toToolCallback(source);
        String schema = callback.getToolDefinition().inputSchema();
        assertTrue(schema.contains("\"operation\""), "FTP schema should have 'operation': " + schema);
        assertTrue(schema.contains("\"path\""), "FTP schema should have 'path': " + schema);
        String name = callback.getToolDefinition().name();
        assertTrue(name.startsWith("file_op_"), "FTP tool name should start with file_op_: " + name);
    }

    @Test
    @Order(14)
    void shouldIncludeProtocolGuidanceInDescription() {
        AgentDataSource jdbc = newDataSource("ds-tool-desc-jdbc", true);
        ToolCallback jdbcCb = factory.toToolCallback(jdbc);
        assertTrue(jdbcCb.getToolDefinition().description().contains("sql"),
            "JDBC description should mention sql parameter");

        AgentDataSource http = newHttpSource("ds-tool-desc-http");
        ToolCallback httpCb = factory.toToolCallback(http);
        assertTrue(httpCb.getToolDefinition().description().contains("method"),
            "HTTP description should mention method parameter");
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

    private static String sanitize(String s) {
        if (s == null) return "anon";
        return s.replaceAll("[^a-zA-Z0-9_]", "_");
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

    private static AgentDataSource newHttpSource(String name) {
        AgentDataSource s = newDataSource(name, true);
        s.setProtocol(AgentDataSourceProtocol.HTTP);
        s.setEndpointUri("http://localhost:8080/api");
        return s;
    }

    private static AgentDataSource newMongoSource(String name) {
        AgentDataSource s = newDataSource(name, true);
        s.setProtocol(AgentDataSourceProtocol.MONGODB);
        s.setEndpointUri("mongodb://localhost:27017/testdb");
        return s;
    }

    private static AgentDataSource newRedisSource(String name) {
        AgentDataSource s = newDataSource(name, true);
        s.setProtocol(AgentDataSourceProtocol.REDIS);
        s.setEndpointUri("redis://localhost:6379");
        return s;
    }

    private static AgentDataSource newKafkaSource(String name) {
        AgentDataSource s = newDataSource(name, true);
        s.setProtocol(AgentDataSourceProtocol.KAFKA);
        s.setEndpointUri("kafka:events?brokers=localhost:9092");
        return s;
    }

    private static AgentDataSource newFtpSource(String name) {
        AgentDataSource s = newDataSource(name, true);
        s.setProtocol(AgentDataSourceProtocol.FTP);
        s.setEndpointUri("ftp://ftp.example.com/data?username=user&password=pass");
        return s;
    }
}
