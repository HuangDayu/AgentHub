/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agenthub.infrastructure.agents.alibaba.saver;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.Checkpoint;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.serializer.StateSerializer;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;


/**
 * copy to com.alibaba.cloud.ai.graph.checkpoint.savers.postgresql.PostgresSaver
 */
public class AgentHubPostgresSaver extends MemorySaver {
    private static final Logger log = LoggerFactory.getLogger(AgentHubPostgresSaver.class);
    /**
     * Datasource used to create the store
     */
    protected final DataSource datasource;

    private final StateSerializer stateSerializer;

    protected AgentHubPostgresSaver(Builder builder) throws SQLException {
        this.datasource = builder.datasource;
        this.stateSerializer = builder.stateSerializer;
        initTable(builder.dropTablesFirst, builder.createTables);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void rollback(Connection conn, Checkpoint checkpoint, String threadId) {
        if (conn == null) return;

        requireNonNull(checkpoint, "checkpoint cannot be null");

        try {
            conn.rollback();
            log.warn("Transaction rolled back for checkpoint {}", checkpoint.getId());
        } catch (SQLException exRollback) {
            log.error("Failed to rollback transaction for checkpoint id {} in thread {}",
                    checkpoint.getId(),
                    threadId,
                    exRollback);
        }
    }

    private String encodeState(Map<String, Object> data) throws IOException {
        var binaryData = stateSerializer.dataToBytes(data);
        var base64Data = Base64.getEncoder().encodeToString(binaryData);
        return format("""
                {"binaryPayload": "%s"}
                """, base64Data);
    }

    private Map<String, Object> decodeState(byte[] binaryPayload, String contentType) throws IOException, ClassNotFoundException {
        if (!Objects.equals(contentType, stateSerializer.contentType())) {
            throw new IllegalStateException(
                    format("Content Type used for store state '%s' is different from one '%s' used for deserialize it",
                            contentType,
                            stateSerializer.contentType()));
        }

        byte[] bytes = Base64.getDecoder().decode(binaryPayload);
        return stateSerializer.dataFromBytes(bytes);
    }

    protected void initTable(boolean dropTablesFirst, boolean createTables) throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            if (dropTablesFirst) {
                runSchemaCommand(statement, SQL_DROP_TABLES, "drop tables");
            }
            if (createTables) {
                runCreateTablesCommand(statement);
            }
        }
    }

    private static final String SQL_DROP_TABLES = """
            DROP TABLE IF EXISTS GraphCheckpoint CASCADE;
            DROP TABLE IF EXISTS GraphThread CASCADE;
            """;

    private static final String SQL_CREATE_TABLES = """
            CREATE TABLE IF NOT EXISTS GraphThread (
                 thread_id UUID PRIMARY KEY,
                 thread_name VARCHAR(255),
                 is_released BOOLEAN DEFAULT FALSE NOT NULL
             );

             CREATE TABLE IF NOT EXISTS GraphCheckpoint (
                 checkpoint_id UUID PRIMARY KEY,
                 parent_checkpoint_id UUID,
                 thread_id UUID NOT NULL,
                 node_id VARCHAR(255),
                 next_node_id VARCHAR(255),
                 state_data JSONB NOT NULL,
                 state_content_type VARCHAR(100) NOT NULL,
                 saved_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                 CONSTRAINT fk_thread
                     FOREIGN KEY(thread_id)
                     REFERENCES GraphThread(thread_id)
                     ON DELETE CASCADE
             );

             CREATE INDEX idx_lg4jcheckpoint_thread_id ON GraphCheckpoint(thread_id);
             CREATE INDEX idx_lg4jcheckpoint_thread_id_saved_at_desc ON GraphCheckpoint(thread_id, saved_at DESC);
             CREATE UNIQUE INDEX idx_unique_lg4jthread_thread_name_unreleased  ON GraphThread(thread_name) WHERE is_released = FALSE;
            """;

    private void runSchemaCommand(Statement statement, String sql, String label) throws SQLException {
        try {
            log.trace("Executing {}:\n---\n{}---", label, sql);
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            log.error("error executing command\n{}\n", sql, ex);
            throw ex;
        }
    }

    private void runCreateTablesCommand(Statement statement) {
        try {
            runSchemaCommand(statement, SQL_CREATE_TABLES, "create tables");
        } catch (SQLException e) {
            log.error("error executing create tables command : {}", e.getMessage());
        }
    }

    @Override
    protected LinkedList<Checkpoint> loadedCheckpoints(RunnableConfig config, LinkedList<Checkpoint> checkpoints) throws Exception {

        if (!checkpoints.isEmpty()) return checkpoints;

        var threadId = config.threadId().orElse(THREAD_ID_DEFAULT);

        try (Connection conn = getConnection()) {
            loadCheckpointsForActiveThread(conn, threadId, checkpoints);
        }

        return checkpoints;
    }

    private void loadCheckpointsForActiveThread(Connection conn, String threadId, LinkedList<Checkpoint> checkpoints) throws SQLException, IOException, ClassNotFoundException {
        int count = countActiveThread(conn, threadId);
        if (count == 0) return;
        if (count > 1) {
            throw new IllegalStateException(format("there are more than one Thread '%s' open (not released yet)", threadId));
        }
        loadCheckpointRows(conn, threadId, checkpoints);
    }

    private int countActiveThread(Connection conn, String threadId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ACTIVE_THREAD)) {
            ps.setString(1, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void loadCheckpointRows(Connection conn, String threadId, LinkedList<Checkpoint> checkpoints) throws SQLException, IOException, ClassNotFoundException {
        log.trace("Executing select checkpoints:\n---\n{}---", SQL_QUERY_CHECKPOINTS);
        try (PreparedStatement ps = conn.prepareStatement(SQL_QUERY_CHECKPOINTS)) {
            ps.setString(1, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    checkpoints.add(buildCheckpointFromRow(rs));
                }
            }
        }
    }

    private Checkpoint buildCheckpointFromRow(ResultSet rs) throws SQLException, IOException, ClassNotFoundException {
        return Checkpoint.builder()
                .id(rs.getString(1))
                .nodeId(rs.getString(2))
                .nextNodeId(rs.getString(3))
                .state(decodeState(rs.getBytes(4), rs.getString(5)))
                .build();
    }

    private static final String SQL_COUNT_ACTIVE_THREAD = """
            SELECT COUNT(*)
            FROM GraphThread
            WHERE thread_name = ? AND is_released = FALSE
            """;

    private static final String SQL_QUERY_CHECKPOINTS = """
            WITH matched_thread AS (
                SELECT thread_id
                FROM GraphThread
                WHERE thread_name = ? AND is_released = FALSE
            )
            SELECT  c.checkpoint_id,
                    c.node_id,
                    c.next_node_id,
                    c.state_data->>'binaryPayload' AS base64_data,
                    c.state_content_type,
                    c.parent_checkpoint_id
            FROM matched_thread t
            JOIN GraphCheckpoint c ON c.thread_id = t.thread_id
            ORDER BY c.saved_at DESC
            """;

    private void insertCheckpoint(Connection conn, RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint) throws Exception {
        var threadId = config.threadId().orElse(THREAD_ID_DEFAULT);
        UUID threadUUID = upsertThread(conn, threadId);
        insertCheckpointRow(conn, checkpoint, threadUUID);
    }

    /**
     * 在 GraphThread 表上 upsert 一条 thread 记录并返回其 UUID。
     */
    private UUID upsertThread(Connection conn, String threadId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPSERT_THREAD)) {
            setUpsertThreadParams(ps, threadId);
            log.trace("Executing upsert thread:\n---\n{}---", SQL_UPSERT_THREAD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject("thread_id", UUID.class) : null;
            }
        }
    }

    private void setUpsertThreadParams(PreparedStatement ps, String threadId) throws SQLException {
        var field = 0;
        ps.setObject(++field, UUID.randomUUID(), Types.OTHER);
        ps.setString(++field, threadId);
        ps.setString(++field, threadId);
    }

    /**
     * 向 GraphCheckpoint 表插入一条 checkpoint 记录。
     */
    private void insertCheckpointRow(Connection conn, Checkpoint checkpoint, UUID threadUUID) throws SQLException, IOException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_CHECKPOINT)) {
            var field = 0;
            ps.setObject(++field, UUID.fromString(checkpoint.getId()), Types.OTHER);
            ps.setNull(++field, Types.OTHER);
            ps.setObject(++field, requireNonNull(threadUUID, "threadUUID cannot be null"), Types.OTHER);
            ps.setString(++field, checkpoint.getNodeId());
            ps.setString(++field, checkpoint.getNextNodeId());
            ps.setString(++field, encodeState(checkpoint.getState()));
            ps.setString(++field, stateSerializer.contentType());
            log.trace("Executing insert checkpoint:\n---\n{}---", SQL_INSERT_CHECKPOINT);
            ps.executeUpdate();
        }
    }

    private static final String SQL_UPSERT_THREAD = """
            WITH inserted AS (
                INSERT INTO GraphThread (thread_id, thread_name, is_released)
                VALUES (?, ?, FALSE)
                ON CONFLICT (thread_name)
                WHERE is_released = FALSE
                DO NOTHING
                RETURNING thread_id
            )
            SELECT thread_id FROM inserted
            UNION ALL
            SELECT thread_id FROM GraphThread
            WHERE thread_name = ? AND is_released = FALSE
            LIMIT 1;
            """;

    private static final String SQL_INSERT_CHECKPOINT = """
            INSERT INTO GraphCheckpoint(
            checkpoint_id,
            parent_checkpoint_id,
            thread_id,
            node_id,
            next_node_id,
            state_data,
            state_content_type)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)
            """;

    @Override
    protected void insertedCheckpoint(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Checkpoint checkpoint) throws Exception {
        var threadId = config.threadId().orElse(THREAD_ID_DEFAULT);

        Connection conn = null;
        try (Connection ignored = conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            insertCheckpoint(conn, config, checkpoints, checkpoint);

            conn.commit();
            log.debug("Checkpoint {} for thread {} inserted successfully.", checkpoint.getId(), threadId);

        } catch (SQLException | IOException e) { // IOException from convertStateToJson
            log.error("Error inserting checkpoint with id {} in thread {}", checkpoint.getId(), threadId, e);
            rollback(conn, checkpoint, threadId);
            throw e;
        }

    }

    @Override
    protected void updatedCheckpoint(RunnableConfig config,
                                     LinkedList<Checkpoint> checkpoints,
                                     Checkpoint checkpoint) throws Exception {
        final var threadId = config.threadId().orElse(THREAD_ID_DEFAULT);
        Connection conn = null;
        CheckpointPersistSpec spec = new CheckpointPersistSpec(config, checkpoints, checkpoint, threadId);
        try (Connection ignored = conn = getConnection()) {
            persistCheckpoint(conn, spec);
        } catch (SQLException | IOException e) {
            handleCheckpointFailure(e, conn, spec);
            throw e;
        }
    }

    private void persistCheckpoint(Connection conn, CheckpointPersistSpec spec) throws Exception {
        conn.setAutoCommit(false);
        deletePreviousCheckpointIfPresent(conn, spec.config(), spec.threadId());
        insertCheckpoint(conn, spec.config(), spec.checkpoints(), spec.checkpoint());
        conn.commit();
        logInsertSuccess(spec.checkpoint(), spec.threadId());
    }

    private void handleCheckpointFailure(Exception e, Connection conn, CheckpointPersistSpec spec) throws SQLException {
        logInsertFailure(e, spec.checkpoint(), spec.threadId());
        rollback(conn, spec.checkpoint(), spec.threadId());
    }

    private static final class CheckpointPersistSpec {
        private final RunnableConfig config;
        private final LinkedList<Checkpoint> checkpoints;
        private final Checkpoint checkpoint;
        private final String threadId;

        CheckpointPersistSpec(RunnableConfig config, LinkedList<Checkpoint> checkpoints,
                              Checkpoint checkpoint, String threadId) {
            this.config = config;
            this.checkpoints = checkpoints;
            this.checkpoint = checkpoint;
            this.threadId = threadId;
        }

        RunnableConfig config() { return config; }
        LinkedList<Checkpoint> checkpoints() { return checkpoints; }
        Checkpoint checkpoint() { return checkpoint; }
        String threadId() { return threadId; }
    }

    private void logInsertFailure(Exception e, Checkpoint checkpoint, String threadId) {
        log.error("Error inserting checkpoint with id {} in thread {}",
                checkpoint.getId(),
                threadId,
                e);
    }

    private void logInsertSuccess(Checkpoint checkpoint, String threadId) {
        log.debug("Checkpoint with id {} for thread {} inserted successfully.",
                checkpoint.getId(),
                threadId);
    }

    /**
     * 当配置中带有 checkPointId 时，删除对应的旧 checkpoint 记录。
     */
    private void deletePreviousCheckpointIfPresent(Connection conn, RunnableConfig config, String threadId) throws SQLException {
        if (config.checkPointId().isEmpty()) {
            return;
        }
        String previousId = config.checkPointId().get();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE_PREVIOUS_CHECKPOINT)) {
            ps.setObject(1, UUID.fromString(previousId), Types.OTHER);
            log.trace("Executing deleting previous checkpoint with id {} in thread {}:\n---\n{}---",
                    previousId, threadId, SQL_DELETE_PREVIOUS_CHECKPOINT);
            ps.executeUpdate();
        }
    }

    private static final String SQL_DELETE_PREVIOUS_CHECKPOINT = """
            DELETE FROM GraphCheckpoint
            WHERE checkpoint_id = ?;
            """;

    @Override
    protected void releasedCheckpoints(RunnableConfig config, LinkedList<Checkpoint> checkpoints, Tag releaseTag) throws Exception {
        var threadId = config.threadId().orElse(THREAD_ID_DEFAULT);

        try (Connection conn = getConnection()) {
            UUID threadUUID = findActiveThread(conn, threadId);
            markThreadReleased(conn, threadUUID);
        }
    }

    /**
     * 查找 threadId 对应的活动 thread 记录，确保唯一性后返回其 UUID。
     */
    private UUID findActiveThread(Connection conn, String threadId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ACTIVE_THREAD)) {
            ps.setString(1, threadId);
            try (ResultSet rs = ps.executeQuery()) {
                return readActiveThreadUuid(rs, threadId);
            }
        }
    }

    private UUID readActiveThreadUuid(ResultSet rs, String threadId) throws SQLException {
        UUID threadUUID = null;
        int rows = 0;
        while (rs.next()) {
            threadUUID = rs.getObject("thread_id", UUID.class);
            ++rows;
        }
        validateThreadRowCount(rows, threadId);
        return threadUUID;
    }

    private void validateThreadRowCount(int rows, String threadId) {
        if (rows == 0) {
            throw new IllegalStateException(format("active Thread '%s' not found", threadId));
        }
        if (rows > 1) {
            throw new IllegalStateException(format("duplicate active Thread '%s' found", threadId));
        }
    }

    /**
     * 将 thread 标记为已释放。
     */
    private void markThreadReleased(Connection conn, UUID threadUUID) throws SQLException {
        log.trace("Executing release Thread:\n---\n{}---", SQL_RELEASE_THREAD);
        try (PreparedStatement ps = conn.prepareStatement(SQL_RELEASE_THREAD)) {
            ps.setObject(1, Objects.requireNonNull(threadUUID, "threadUUID cannot be null"), Types.OTHER);
            ps.executeUpdate();
        }
    }

    private static final String SQL_SELECT_ACTIVE_THREAD = """
            SELECT thread_id FROM GraphThread
            WHERE thread_name = ? AND is_released = FALSE
            """;

    private static final String SQL_RELEASE_THREAD = """
            UPDATE GraphThread
            SET
                is_released = TRUE
            WHERE thread_id = ?;
            """;

    /**
     * Datasource connection
     * Creates the vector extension and add the vector type if it does not exist.
     * Could be overridden in case extension creation and adding type is done at datasource initialization step.
     *
     * @return Datasource connection
     * @throws SQLException exception
     */
    protected Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    public static class Builder extends MemorySaver.Builder {
        public StateSerializer stateSerializer;
        private String host;
        private Integer port;
        private String user;
        private String password;
        private String database;
        private boolean createTables;
        private boolean dropTablesFirst;
        private DataSource datasource;

        public Builder stateSerializer(StateSerializer stateSerializer) {
            this.stateSerializer = stateSerializer;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder datasource(DataSource datasource) {
            this.datasource = datasource;
            return this;
        }

        public Builder createTables(boolean createTables) {
            this.createTables = createTables;
            return this;
        }

        public Builder dropTablesFirst(boolean dropTablesFirst) {
            this.dropTablesFirst = dropTablesFirst;
            return this;
        }

        private String requireNotBlank(String value, String name) {
            if (requireNonNull(value, format("'%s' cannot be null", name)).isBlank()) {
                throw new IllegalArgumentException(format("'%s' cannot be blank", name));
            }
            return value;
        }

        public AgentHubPostgresSaver build() {
            if (stateSerializer == null) {
                log.info("No StateSerializer for saver provided, using default SpringAiJacksonStateSerializer, please make sure saver uses the same serializer of the graph.");
                this.stateSerializer = StateGraph.DEFAULT_JACKSON_SERIALIZER;
            }
            if (datasource == null) {
                if (port <= 0) {
                    throw new IllegalArgumentException("port must be greater than 0");
                }
                var ds = new PGSimpleDataSource();
                ds.setDatabaseName(requireNotBlank(database, "database"));
                ds.setUser(requireNotBlank(user, "user"));
                ds.setPassword(requireNonNull(password, "password cannot be null"));
                ds.setPortNumbers(new int[]{port});
                ds.setServerNames(new String[]{requireNotBlank(host, "host")});

                datasource = ds;
            }

            createTables = createTables || dropTablesFirst;

            try {
                return new AgentHubPostgresSaver(this);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

