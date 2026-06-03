-- =========================================================
-- AgentHub - Auto-generated Schema
-- Generated: 2026-06-02T08:47:53.057982900Z
-- Source: MyBatis-Plus Entity Classes
-- =========================================================

CREATE SCHEMA IF NOT EXISTS app;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
SET search_path TO app, public;

-- =========================================================
-- Drop existing tables (dependency order)
-- =========================================================
-- DROP TABLE IF EXISTS workspace CASCADE;
-- DROP TABLE IF EXISTS workflow_stage CASCADE;
-- DROP TABLE IF EXISTS vector_store_config CASCADE;
-- DROP TABLE IF EXISTS user_input_requests CASCADE;
-- DROP TABLE IF EXISTS traces CASCADE;
-- DROP TABLE IF EXISTS tool_policy_binding CASCADE;
-- DROP TABLE IF EXISTS tool_policy CASCADE;
-- DROP TABLE IF EXISTS tenant CASCADE;
-- DROP TABLE IF EXISTS system_tools CASCADE;
-- DROP TABLE IF EXISTS subsession CASCADE;
-- DROP TABLE IF EXISTS subagent CASCADE;
-- DROP TABLE IF EXISTS spans CASCADE;
-- DROP TABLE IF EXISTS skill_file CASCADE;
-- DROP TABLE IF EXISTS skill_config CASCADE;
-- DROP TABLE IF EXISTS skill CASCADE;
-- DROP TABLE IF EXISTS session CASCADE;
-- DROP TABLE IF EXISTS scheduled_task CASCADE;
-- DROP TABLE IF EXISTS run_registrations CASCADE;
-- DROP TABLE IF EXISTS retrieval_policy CASCADE;
-- DROP TABLE IF EXISTS prompt_template CASCADE;
-- DROP TABLE IF EXISTS node_execution_result CASCADE;
-- DROP TABLE IF EXISTS model_policy CASCADE;
-- DROP TABLE IF EXISTS model_config CASCADE;
-- DROP TABLE IF EXISTS metrics CASCADE;
-- DROP TABLE IF EXISTS message_pushes CASCADE;
-- DROP TABLE IF EXISTS memory CASCADE;
-- DROP TABLE IF EXISTS mcp_tool CASCADE;
-- DROP TABLE IF EXISTS kv_zset CASCADE;
-- DROP TABLE IF EXISTS kv_store CASCADE;
-- DROP TABLE IF EXISTS kv_set CASCADE;
-- DROP TABLE IF EXISTS kv_list CASCADE;
-- DROP TABLE IF EXISTS kv_hash CASCADE;
-- DROP TABLE IF EXISTS knowledge_base CASCADE;
-- DROP TABLE IF EXISTS ingestion_job CASCADE;
-- DROP TABLE IF EXISTS ingestion_document CASCADE;
-- DROP TABLE IF EXISTS iam_refresh_token_session CASCADE;
-- DROP TABLE IF EXISTS http_tools CASCADE;
-- DROP TABLE IF EXISTS guardrail_policy CASCADE;
-- DROP TABLE IF EXISTS dynamic_workflow CASCADE;
-- DROP TABLE IF EXISTS document_chunk CASCADE;
-- DROP TABLE IF EXISTS dag_workflow_execution CASCADE;
-- DROP TABLE IF EXISTS dag_workflow CASCADE;
-- DROP TABLE IF EXISTS chat_message CASCADE;
-- DROP TABLE IF EXISTS app_user CASCADE;
-- DROP TABLE IF EXISTS alerts CASCADE;
-- DROP TABLE IF EXISTS agent_team CASCADE;
-- DROP TABLE IF EXISTS agent_task CASCADE;
-- DROP TABLE IF EXISTS agent_plan_step CASCADE;
-- DROP TABLE IF EXISTS agent_execution_plan CASCADE;
-- DROP TABLE IF EXISTS agent_config CASCADE;
-- DROP TABLE IF EXISTS agent CASCADE;

-- =========================================================
-- Create tables
-- =========================================================

-- Table: agent
CREATE TABLE IF NOT EXISTS agent
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    agent_code   varchar(255),
    name         varchar(255),
    description  text,
    status       varchar(255),
    enabled      boolean,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   varchar(255),
    updated_by   varchar(255),
    PRIMARY KEY (id)
);

-- Table: agent_config
CREATE TABLE IF NOT EXISTS agent_config
(
    id          varchar(64) NOT NULL,
    agent_id    varchar(255),
    category    varchar(255),
    type        varchar(255),
    config_id   varchar(255),
    name        varchar(255),
    description text,
    priority    integer,
    enabled     boolean,
    created_at  timestamptz,
    updated_at  timestamptz,
    PRIMARY KEY (id)
);

-- Table: agent_execution_plan
CREATE TABLE IF NOT EXISTS agent_execution_plan
(
    id                 varchar(64) NOT NULL,
    agent_id           varchar(255),
    session_id         varchar(255),
    goal               varchar(255),
    status             varchar(255),
    current_step_index integer,
    result             varchar(255),
    created_at         timestamptz,
    updated_at         timestamptz,
    PRIMARY KEY (id)
);

-- Table: agent_plan_step
CREATE TABLE IF NOT EXISTS agent_plan_step
(
    id            varchar(64) NOT NULL,
    plan_id       varchar(255),
    step_order    integer,
    description   text,
    tool_name     varchar(255),
    tool_input    varchar(255),
    status        varchar(255),
    output        varchar(255),
    subagent_id   varchar(255),
    subsession_id varchar(255),
    depends_on    varchar(255),
    created_at    timestamptz,
    updated_at    timestamptz,
    PRIMARY KEY (id)
);

-- Table: agent_task
CREATE TABLE IF NOT EXISTS agent_task
(
    id               varchar(64) NOT NULL,
    stage_id         varchar(255),
    workflow_id      varchar(255),
    task_description text,
    subagent_id      varchar(255),
    subsession_id    varchar(255),
    status           varchar(255),
    result           varchar(255),
    model_config_id  varchar(255),
    tool_names       varchar(255),
    created_at       timestamptz,
    updated_at       timestamptz,
    PRIMARY KEY (id)
);

-- Table: agent_team
CREATE TABLE IF NOT EXISTS agent_team
(
    id                varchar(64) NOT NULL,
    tenant_id         varchar(255),
    workspace_id      varchar(255),
    team_code         varchar(255),
    name              varchar(255),
    description       text,
    coordination_mode varchar(255),
    member_config     varchar(255),
    status            varchar(255),
    created_at        timestamptz,
    updated_at        timestamptz,
    PRIMARY KEY (id)
);

-- Table: alerts
CREATE TABLE IF NOT EXISTS alerts
(
    id           varchar(64) NOT NULL,
    alert_level  varchar(255),
    alert_type   varchar(255),
    title        varchar(255),
    message      varchar(255),
    run_id       varchar(255),
    agent_id     varchar(255),
    trace_id     varchar(255),
    metadata     text,
    resolved     boolean,
    resolved_at  timestamptz,
    resolved_by  varchar(255),
    tenant_id    varchar(255),
    workspace_id varchar(255),
    created_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: app_user
CREATE TABLE IF NOT EXISTS app_user
(
    id            varchar(64) NOT NULL,
    username      varchar(255),
    password_hash varchar(255),
    tenant_id     varchar(255),
    status        varchar(255),
    email         varchar(255),
    display_name  varchar(255),
    auth_source   varchar(255),
    created_at    timestamptz,
    updated_at    timestamptz,
    PRIMARY KEY (id)
);

-- Table: chat_message
CREATE TABLE IF NOT EXISTS chat_message
(
    id         varchar(64) NOT NULL,
    session_id varchar(255),
    role       varchar(255),
    content    text,
    created_at timestamptz,
    PRIMARY KEY (id)
);

-- Table: dag_workflow
CREATE TABLE IF NOT EXISTS dag_workflow
(
    id               varchar(64) NOT NULL,
    tenant_id        varchar(255),
    workspace_id     varchar(255),
    workflow_code    varchar(255),
    name             varchar(255),
    description      text,
    graph_definition text,
    status           varchar(255),
    created_at       timestamptz,
    updated_at       timestamptz,
    PRIMARY KEY (id)
);

-- Table: dag_workflow_execution
CREATE TABLE IF NOT EXISTS dag_workflow_execution
(
    id           varchar(64) NOT NULL,
    workflow_id  varchar(255),
    tenant_id    varchar(255),
    workspace_id varchar(255),
    execution_id varchar(255),
    status       varchar(255),
    input        varchar(255),
    output       varchar(255),
    error_info   varchar(255),
    start_time   timestamptz,
    end_time     timestamptz,
    duration     bigint,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: document_chunk
CREATE TABLE IF NOT EXISTS document_chunk
(
    id          varchar(64) NOT NULL,
    chunk_id    varchar(255),
    document_id varchar(255),
    kb_id       varchar(255),
    chunk_index integer,
    token_count integer,
    PRIMARY KEY (id)
);

-- Table: dynamic_workflow
CREATE TABLE IF NOT EXISTS dynamic_workflow
(
    id                    varchar(64) NOT NULL,
    agent_id              varchar(255),
    session_id            varchar(255),
    task                  varchar(255),
    pattern               varchar(255),
    status                varchar(255),
    result                varchar(255),
    max_concurrent_agents integer,
    total_tokens_used     integer,
    created_at            timestamptz,
    updated_at            timestamptz,
    PRIMARY KEY (id)
);

-- Table: guardrail_policy
CREATE TABLE IF NOT EXISTS guardrail_policy
(
    id                         varchar(64) NOT NULL,
    tenant_id                  varchar(255),
    workspace_id               varchar(255),
    name                       varchar(255),
    description                text,
    input_validation_enabled   boolean,
    output_validation_enabled  boolean,
    pii_detection_enabled      boolean,
    pii_masking_enabled        boolean,
    prompt_injection_detection boolean,
    max_input_length           integer,
    max_output_length          integer,
    created_at                 timestamptz,
    updated_at                 timestamptz,
    PRIMARY KEY (id)
);

-- Table: http_tools
CREATE TABLE IF NOT EXISTS http_tools
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    name         varchar(255),
    description  text,
    enabled      boolean,
    endpoint     varchar(255),
    auth_type    varchar(255),
    input_schema varchar(255),
    timeout_ms   integer,
    created_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: iam_refresh_token_session
CREATE TABLE IF NOT EXISTS iam_refresh_token_session
(
    token      varchar(255) NOT NULL,
    subject    varchar(255),
    expires_at timestamptz,
    PRIMARY KEY (token)
);

-- Table: ingestion_document
CREATE TABLE IF NOT EXISTS ingestion_document
(
    id           varchar(64) NOT NULL,
    kb_id        varchar(255),
    job_id       varchar(255),
    file_name    varchar(255),
    content_type text,
    size         bigint,
    storage_path varchar(255),
    status       varchar(255),
    PRIMARY KEY (id)
);

-- Table: ingestion_job
CREATE TABLE IF NOT EXISTS ingestion_job
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    kb_id           varchar(255),
    document_id     varchar(255),
    trigger_type    varchar(255),
    status          varchar(255),
    progress        integer,
    parser_name     varchar(255),
    embedding_model varchar(255),
    index_version   integer,
    document_count  integer,
    error_code      varchar(255),
    error_message   varchar(255),
    started_at      timestamptz,
    ended_at        timestamptz,
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- Table: knowledge_base
CREATE TABLE IF NOT EXISTS knowledge_base
(
    id                        varchar(64) NOT NULL,
    tenant_id                 varchar(255),
    workspace_id              varchar(255),
    kb_code                   varchar(255),
    name                      varchar(255),
    description               text,
    status                    varchar(255),
    created_at                timestamptz,
    created_by                varchar(255),
    updated_at                timestamptz,
    updated_by                varchar(255),
    vector_store_config_id    varchar(255),
    embedding_model_config_id varchar(255),
    chat_model_config_id      varchar(255),
    PRIMARY KEY (id)
);

-- Table: kv_hash
CREATE TABLE IF NOT EXISTS kv_hash
(
    kv_key   varchar(255) NOT NULL,
    field    varchar(255),
    kv_value varchar(255),
    PRIMARY KEY (kv_key)
);

-- Table: kv_list
CREATE TABLE IF NOT EXISTS kv_list
(
    kv_key     varchar(255) NOT NULL,
    list_index bigint,
    kv_value   varchar(255),
    PRIMARY KEY (kv_key)
);

-- Table: kv_set
CREATE TABLE IF NOT EXISTS kv_set
(
    kv_key varchar(255) NOT NULL,
    member varchar(255),
    PRIMARY KEY (kv_key)
);

-- Table: kv_store
CREATE TABLE IF NOT EXISTS kv_store
(
    kv_key      varchar(255) NOT NULL,
    kv_value    varchar(255),
    kv_type     varchar(255),
    expire_time bigint,
    created_at  timestamptz,
    updated_at  timestamptz,
    PRIMARY KEY (kv_key)
);

-- Table: kv_zset
CREATE TABLE IF NOT EXISTS kv_zset
(
    kv_key varchar(255) NOT NULL,
    member varchar(255),
    score  double precision,
    PRIMARY KEY (kv_key)
);

-- Table: mcp_tool
CREATE TABLE IF NOT EXISTS mcp_tool
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    name         varchar(255),
    description  text,
    server_url   text,
    server_type  varchar(255),
    command      varchar(255),
    args         varchar(255),
    env          varchar(255),
    async        boolean,
    enabled      boolean,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: memory
CREATE TABLE IF NOT EXISTS memory
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    agent_id     varchar(255),
    name         varchar(255),
    memory_type  varchar(255),
    content      text,
    metadata     varchar(255),
    importance   double precision,
    expires_at   timestamptz,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: message_pushes
CREATE TABLE IF NOT EXISTS message_pushes
(
    id         varchar(64) NOT NULL,
    message_id varchar(255),
    run_id     varchar(255),
    role       varchar(255),
    content    text,
    metadata   text,
    timestamp  timestamptz,
    created_at timestamptz,
    PRIMARY KEY (id)
);

-- Table: metrics
CREATE TABLE IF NOT EXISTS metrics
(
    id           varchar(64) NOT NULL,
    metric_type  varchar(255),
    metric_name  varchar(255),
    metric_value double precision,
    run_id       varchar(255),
    agent_id     varchar(255),
    trace_id     varchar(255),
    span_id      varchar(255),
    labels       text,
    timestamp    timestamptz,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    created_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: model_config
CREATE TABLE IF NOT EXISTS model_config
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    name         varchar(255),
    type         varchar(255),
    supplier     varchar(255),
    api_key      varchar(255),
    base_url     text,
    model        varchar(255),
    enabled      boolean,
    created_at   timestamptz,
    updated_at   timestamptz,
    created_by   varchar(255),
    PRIMARY KEY (id)
);

-- Table: model_policy
CREATE TABLE IF NOT EXISTS model_policy
(
    id                varchar(64) NOT NULL,
    tenant_id         varchar(255),
    workspace_id      varchar(255),
    name              varchar(255),
    description       text,
    temperature       double precision,
    max_tokens        integer,
    top_p             double precision,
    frequency_penalty double precision,
    presence_penalty  double precision,
    created_at        timestamptz,
    updated_at        timestamptz,
    PRIMARY KEY (id)
);

-- Table: node_execution_result
CREATE TABLE IF NOT EXISTS node_execution_result
(
    id           varchar(64) NOT NULL,
    execution_id varchar(255),
    node_id      varchar(255),
    node_name    varchar(255),
    node_type    varchar(255),
    status       varchar(255),
    input        varchar(255),
    output       varchar(255),
    error_info   varchar(255),
    start_time   timestamptz,
    end_time     timestamptz,
    duration     bigint,
    created_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: prompt_template
CREATE TABLE IF NOT EXISTS prompt_template
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    name         varchar(255),
    description  text,
    category     varchar(255),
    content      text,
    variables    varchar(255),
    active       boolean,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: retrieval_policy
CREATE TABLE IF NOT EXISTS retrieval_policy
(
    id                   varchar(64) NOT NULL,
    tenant_id            varchar(255),
    workspace_id         varchar(255),
    name                 varchar(255),
    description          text,
    retrieval_type       varchar(255),
    top_k                integer,
    score_threshold      double precision,
    enable_rerank        boolean,
    enable_query_rewrite boolean,
    enable_text_search   boolean,
    enable_vector_search boolean,
    rerank_model         varchar(255),
    vector_weight        double precision,
    keyword_weight       double precision,
    created_at           timestamptz,
    updated_at           timestamptz,
    PRIMARY KEY (id)
);

-- Table: run_registrations
CREATE TABLE IF NOT EXISTS run_registrations
(
    id         varchar(64) NOT NULL,
    project    varchar(255),
    name       varchar(255),
    timestamp  timestamptz,
    pid        integer,
    status     varchar(255),
    run_dir    varchar(255),
    created_at timestamptz,
    PRIMARY KEY (id)
);

-- Table: scheduled_task
CREATE TABLE IF NOT EXISTS scheduled_task
(
    id                varchar(64) NOT NULL,
    tenant_id         varchar(255),
    workspace_id      varchar(255),
    task_code         varchar(255),
    name              varchar(255),
    description       text,
    task_type         varchar(255),
    cron_expression   varchar(255),
    executor_config   varchar(255),
    prompt            varchar(255),
    enabled           boolean,
    last_execute_time timestamptz,
    next_execute_time timestamptz,
    status            varchar(255),
    agent_id          varchar(255),
    last_run_result   text,
    run_count         integer,
    created_at        timestamptz,
    updated_at        timestamptz,
    created_by        varchar(255),
    updated_by        varchar(255),
    PRIMARY KEY (id)
);

-- Table: session
CREATE TABLE IF NOT EXISTS session
(
    id           varchar(64) NOT NULL,
    agent_id     varchar(255),
    name         varchar(255),
    tenant_id    varchar(255),
    workspace_id varchar(255),
    created_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: skill
CREATE TABLE IF NOT EXISTS skill
(
    id               varchar(64) NOT NULL,
    tenant_id        varchar(255),
    workspace_id     varchar(255),
    skill_code       varchar(255),
    name             varchar(255),
    description      text,
    skill_type       varchar(255),
    skill_path       text,
    skill_files_tree text,
    source           varchar(255),
    source_path      text,
    zip_storage_path text,
    config_id        varchar(255),
    file_count       integer,
    total_size       bigint,
    enabled          boolean,
    created_at       timestamptz,
    updated_at       timestamptz,
    last_sync_at     timestamptz,
    PRIMARY KEY (id)
);

-- Table: skill_config
CREATE TABLE IF NOT EXISTS skill_config
(
    id            varchar(64) NOT NULL,
    tenant_id     varchar(255),
    workspace_id  varchar(255),
    name          varchar(255),
    description   text,
    skill_paths   text,
    sync_enabled  boolean,
    sync_interval integer,
    auto_sync     boolean,
    enabled       boolean,
    last_sync_at  timestamptz,
    created_at    timestamptz,
    updated_at    timestamptz,
    PRIMARY KEY (id)
);

-- Table: skill_file
CREATE TABLE IF NOT EXISTS skill_file
(
    id           varchar(64) NOT NULL,
    skill_id     varchar(255),
    tenant_id    varchar(255),
    workspace_id varchar(255),
    file_path    varchar(255),
    file_name    varchar(255),
    file_ext     varchar(255),
    file_size    bigint,
    file_type    varchar(255),
    encoding     varchar(255),
    storage_path varchar(255),
    checksum     varchar(255),
    is_directory boolean,
    metadata     text,
    version      integer,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

-- Table: spans
CREATE TABLE IF NOT EXISTS spans
(
    id                   varchar(64) NOT NULL,
    span_id              varchar(255),
    trace_id             varchar(255),
    parent_span_id       varchar(255),
    name                 varchar(255),
    kind                 varchar(255),
    start_time_unix_nano varchar(255),
    end_time_unix_nano   varchar(255),
    latency_ns           bigint,
    attributes           text,
    events               text,
    links                text,
    status_code          integer,
    status_message       varchar(255),
    resource             text,
    scope                text,
    model                varchar(255),
    input_tokens         bigint,
    output_tokens        bigint,
    total_tokens         bigint,
    conversation_id      varchar(255),
    operation_name       varchar(255),
    service_name         varchar(255),
    start_timestamp      bigint,
    end_timestamp        bigint,
    duration             bigint,
    status               varchar(255),
    status_description   text,
    run_id               varchar(255),
    agent_id             varchar(255),
    tenant_id            varchar(255),
    workspace_id         varchar(255),
    created_at           timestamptz,
    PRIMARY KEY (id)
);

-- Table: subagent
CREATE TABLE IF NOT EXISTS subagent
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    parent_agent_id varchar(255),
    name            varchar(255),
    description     text,
    system_prompt   varchar(255),
    model_config_id varchar(255),
    status          varchar(255),
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- Table: subsession
CREATE TABLE IF NOT EXISTS subsession
(
    id                varchar(64) NOT NULL,
    parent_session_id varchar(255),
    subagent_id       varchar(255),
    name              varchar(255),
    status            varchar(255),
    created_at        timestamptz,
    updated_at        timestamptz,
    PRIMARY KEY (id)
);

-- Table: system_tools
CREATE TABLE IF NOT EXISTS system_tools
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    tool_class_name varchar(255),
    tool_name       varchar(255),
    description     text,
    category        varchar(255),
    method_count    integer,
    enabled         boolean,
    system_tool     boolean,
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- Table: tenant
CREATE TABLE IF NOT EXISTS tenant
(
    id              varchar(64) NOT NULL,
    tenant_code     varchar(255),
    name            varchar(255),
    plan_code       varchar(255),
    isolation_level varchar(255),
    status          varchar(255),
    region          varchar(255),
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- Table: tool_policy
CREATE TABLE IF NOT EXISTS tool_policy
(
    id                   varchar(64) NOT NULL,
    tenant_id            varchar(255),
    workspace_id         varchar(255),
    name                 varchar(255),
    description          text,
    max_concurrent_calls integer,
    timeout_seconds      integer,
    retry_count          integer,
    fallback_enabled     boolean,
    created_at           timestamptz,
    updated_at           timestamptz,
    PRIMARY KEY (id)
);

-- Table: tool_policy_binding
CREATE TABLE IF NOT EXISTS tool_policy_binding
(
    id             varchar(64) NOT NULL,
    tool_policy_id varchar(255),
    tool_id        varchar(255),
    priority       integer,
    enabled        boolean,
    created_at     timestamptz,
    PRIMARY KEY (id)
);

-- Table: traces
CREATE TABLE IF NOT EXISTS traces
(
    id                   varchar(64) NOT NULL,
    trace_id             varchar(255),
    run_id               varchar(255),
    root_span_id         varchar(255),
    span_count           integer,
    start_time_unix_nano varchar(255),
    end_time_unix_nano   varchar(255),
    duration_ns          bigint,
    status_code          integer,
    error_message        varchar(255),
    total_tokens         bigint,
    tenant_id            varchar(255),
    workspace_id         varchar(255),
    created_at           timestamptz,
    PRIMARY KEY (id)
);

-- Table: user_input_requests
CREATE TABLE IF NOT EXISTS user_input_requests
(
    id               varchar(64) NOT NULL,
    request_id       varchar(255),
    run_id           varchar(255),
    agent_id         varchar(255),
    agent_name       varchar(255),
    structured_input varchar(255),
    created_at       timestamptz,
    PRIMARY KEY (id)
);

-- Table: vector_store_config
CREATE TABLE IF NOT EXISTS vector_store_config
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    name            varchar(255),
    type            varchar(255),
    host            varchar(255),
    port            integer,
    api_key         varchar(255),
    collection_name varchar(255),
    extra_params    varchar(255),
    enabled         boolean,
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

-- Table: workflow_stage
CREATE TABLE IF NOT EXISTS workflow_stage
(
    id                   varchar(64) NOT NULL,
    workflow_id          varchar(255),
    stage_order          integer,
    name                 varchar(255),
    stage_type           varchar(255),
    system_prompt        varchar(255),
    task_template        varchar(255),
    depends_on           varchar(255),
    status               varchar(255),
    output               varchar(255),
    completed_task_count integer,
    total_task_count     integer,
    created_at           timestamptz,
    updated_at           timestamptz,
    PRIMARY KEY (id)
);

-- Table: workspace
CREATE TABLE IF NOT EXISTS workspace
(
    id             varchar(64) NOT NULL,
    tenant_id      varchar(255),
    workspace_code varchar(255),
    name           varchar(255),
    region         varchar(255),
    status         varchar(255),
    created_at     timestamptz,
    updated_at     timestamptz,
    PRIMARY KEY (id)
);

-- =========================================================
-- Create indexes
-- =========================================================

CREATE INDEX IF NOT EXISTS idx_agent_tenant_id ON agent (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agent_workspace_id ON agent (workspace_id);

CREATE INDEX IF NOT EXISTS idx_agent_config_agent_id ON agent_config (agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_config_config_id ON agent_config (config_id);

CREATE INDEX IF NOT EXISTS idx_agent_execution_plan_agent_id ON agent_execution_plan (agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_execution_plan_session_id ON agent_execution_plan (session_id);

CREATE INDEX IF NOT EXISTS idx_agent_plan_step_plan_id ON agent_plan_step (plan_id);
CREATE INDEX IF NOT EXISTS idx_agent_plan_step_subagent_id ON agent_plan_step (subagent_id);
CREATE INDEX IF NOT EXISTS idx_agent_plan_step_subsession_id ON agent_plan_step (subsession_id);

CREATE INDEX IF NOT EXISTS idx_agent_task_stage_id ON agent_task (stage_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_workflow_id ON agent_task (workflow_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_subagent_id ON agent_task (subagent_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_subsession_id ON agent_task (subsession_id);
CREATE INDEX IF NOT EXISTS idx_agent_task_model_config_id ON agent_task (model_config_id);

CREATE INDEX IF NOT EXISTS idx_agent_team_tenant_id ON agent_team (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agent_team_workspace_id ON agent_team (workspace_id);

CREATE INDEX IF NOT EXISTS idx_alerts_run_id ON alerts (run_id);
CREATE INDEX IF NOT EXISTS idx_alerts_agent_id ON alerts (agent_id);
CREATE INDEX IF NOT EXISTS idx_alerts_trace_id ON alerts (trace_id);
CREATE INDEX IF NOT EXISTS idx_alerts_tenant_id ON alerts (tenant_id);
CREATE INDEX IF NOT EXISTS idx_alerts_workspace_id ON alerts (workspace_id);

CREATE INDEX IF NOT EXISTS idx_app_user_tenant_id ON app_user (tenant_id);

CREATE INDEX IF NOT EXISTS idx_chat_message_session_id ON chat_message (session_id);

CREATE INDEX IF NOT EXISTS idx_dag_workflow_tenant_id ON dag_workflow (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dag_workflow_workspace_id ON dag_workflow (workspace_id);

CREATE INDEX IF NOT EXISTS idx_dag_workflow_execution_workflow_id ON dag_workflow_execution (workflow_id);
CREATE INDEX IF NOT EXISTS idx_dag_workflow_execution_tenant_id ON dag_workflow_execution (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dag_workflow_execution_workspace_id ON dag_workflow_execution (workspace_id);
CREATE INDEX IF NOT EXISTS idx_dag_workflow_execution_execution_id ON dag_workflow_execution (execution_id);

CREATE INDEX IF NOT EXISTS idx_document_chunk_chunk_id ON document_chunk (chunk_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_document_id ON document_chunk (document_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_kb_id ON document_chunk (kb_id);

CREATE INDEX IF NOT EXISTS idx_dynamic_workflow_agent_id ON dynamic_workflow (agent_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_workflow_session_id ON dynamic_workflow (session_id);

CREATE INDEX IF NOT EXISTS idx_guardrail_policy_tenant_id ON guardrail_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_guardrail_policy_workspace_id ON guardrail_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_http_tools_tenant_id ON http_tools (tenant_id);
CREATE INDEX IF NOT EXISTS idx_http_tools_workspace_id ON http_tools (workspace_id);

CREATE INDEX IF NOT EXISTS idx_ingestion_document_kb_id ON ingestion_document (kb_id);
CREATE INDEX IF NOT EXISTS idx_ingestion_document_job_id ON ingestion_document (job_id);

CREATE INDEX IF NOT EXISTS idx_ingestion_job_tenant_id ON ingestion_job (tenant_id);
CREATE INDEX IF NOT EXISTS idx_ingestion_job_workspace_id ON ingestion_job (workspace_id);
CREATE INDEX IF NOT EXISTS idx_ingestion_job_kb_id ON ingestion_job (kb_id);
CREATE INDEX IF NOT EXISTS idx_ingestion_job_document_id ON ingestion_job (document_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_base_tenant_id ON knowledge_base (tenant_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_workspace_id ON knowledge_base (workspace_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_vector_store_config_id ON knowledge_base (vector_store_config_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_embedding_model_config_id ON knowledge_base (embedding_model_config_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_base_chat_model_config_id ON knowledge_base (chat_model_config_id);

CREATE INDEX IF NOT EXISTS idx_mcp_tool_tenant_id ON mcp_tool (tenant_id);
CREATE INDEX IF NOT EXISTS idx_mcp_tool_workspace_id ON mcp_tool (workspace_id);

CREATE INDEX IF NOT EXISTS idx_memory_tenant_id ON memory (tenant_id);
CREATE INDEX IF NOT EXISTS idx_memory_workspace_id ON memory (workspace_id);
CREATE INDEX IF NOT EXISTS idx_memory_agent_id ON memory (agent_id);

CREATE INDEX IF NOT EXISTS idx_message_pushes_message_id ON message_pushes (message_id);
CREATE INDEX IF NOT EXISTS idx_message_pushes_run_id ON message_pushes (run_id);

CREATE INDEX IF NOT EXISTS idx_metrics_run_id ON metrics (run_id);
CREATE INDEX IF NOT EXISTS idx_metrics_agent_id ON metrics (agent_id);
CREATE INDEX IF NOT EXISTS idx_metrics_trace_id ON metrics (trace_id);
CREATE INDEX IF NOT EXISTS idx_metrics_span_id ON metrics (span_id);
CREATE INDEX IF NOT EXISTS idx_metrics_tenant_id ON metrics (tenant_id);
CREATE INDEX IF NOT EXISTS idx_metrics_workspace_id ON metrics (workspace_id);

CREATE INDEX IF NOT EXISTS idx_model_config_tenant_id ON model_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_model_config_workspace_id ON model_config (workspace_id);

CREATE INDEX IF NOT EXISTS idx_model_policy_tenant_id ON model_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_model_policy_workspace_id ON model_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_node_execution_result_execution_id ON node_execution_result (execution_id);
CREATE INDEX IF NOT EXISTS idx_node_execution_result_node_id ON node_execution_result (node_id);

CREATE INDEX IF NOT EXISTS idx_prompt_template_tenant_id ON prompt_template (tenant_id);
CREATE INDEX IF NOT EXISTS idx_prompt_template_workspace_id ON prompt_template (workspace_id);

CREATE INDEX IF NOT EXISTS idx_retrieval_policy_tenant_id ON retrieval_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_retrieval_policy_workspace_id ON retrieval_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_scheduled_task_tenant_id ON scheduled_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_task_workspace_id ON scheduled_task (workspace_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_task_agent_id ON scheduled_task (agent_id);

CREATE INDEX IF NOT EXISTS idx_session_agent_id ON session (agent_id);
CREATE INDEX IF NOT EXISTS idx_session_tenant_id ON session (tenant_id);
CREATE INDEX IF NOT EXISTS idx_session_workspace_id ON session (workspace_id);

CREATE INDEX IF NOT EXISTS idx_skill_tenant_id ON skill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_workspace_id ON skill (workspace_id);
CREATE INDEX IF NOT EXISTS idx_skill_config_id ON skill (config_id);

CREATE INDEX IF NOT EXISTS idx_skill_config_tenant_id ON skill_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_config_workspace_id ON skill_config (workspace_id);

CREATE INDEX IF NOT EXISTS idx_skill_file_skill_id ON skill_file (skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_file_tenant_id ON skill_file (tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_file_workspace_id ON skill_file (workspace_id);

CREATE INDEX IF NOT EXISTS idx_spans_span_id ON spans (span_id);
CREATE INDEX IF NOT EXISTS idx_spans_trace_id ON spans (trace_id);
CREATE INDEX IF NOT EXISTS idx_spans_parent_span_id ON spans (parent_span_id);
CREATE INDEX IF NOT EXISTS idx_spans_conversation_id ON spans (conversation_id);
CREATE INDEX IF NOT EXISTS idx_spans_run_id ON spans (run_id);
CREATE INDEX IF NOT EXISTS idx_spans_agent_id ON spans (agent_id);
CREATE INDEX IF NOT EXISTS idx_spans_tenant_id ON spans (tenant_id);
CREATE INDEX IF NOT EXISTS idx_spans_workspace_id ON spans (workspace_id);

CREATE INDEX IF NOT EXISTS idx_subagent_tenant_id ON subagent (tenant_id);
CREATE INDEX IF NOT EXISTS idx_subagent_workspace_id ON subagent (workspace_id);
CREATE INDEX IF NOT EXISTS idx_subagent_parent_agent_id ON subagent (parent_agent_id);
CREATE INDEX IF NOT EXISTS idx_subagent_model_config_id ON subagent (model_config_id);

CREATE INDEX IF NOT EXISTS idx_subsession_parent_session_id ON subsession (parent_session_id);
CREATE INDEX IF NOT EXISTS idx_subsession_subagent_id ON subsession (subagent_id);

CREATE INDEX IF NOT EXISTS idx_system_tools_tenant_id ON system_tools (tenant_id);
CREATE INDEX IF NOT EXISTS idx_system_tools_workspace_id ON system_tools (workspace_id);

CREATE INDEX IF NOT EXISTS idx_tool_policy_tenant_id ON tool_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tool_policy_workspace_id ON tool_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_tool_policy_binding_tool_policy_id ON tool_policy_binding (tool_policy_id);
CREATE INDEX IF NOT EXISTS idx_tool_policy_binding_tool_id ON tool_policy_binding (tool_id);

CREATE INDEX IF NOT EXISTS idx_traces_trace_id ON traces (trace_id);
CREATE INDEX IF NOT EXISTS idx_traces_run_id ON traces (run_id);
CREATE INDEX IF NOT EXISTS idx_traces_root_span_id ON traces (root_span_id);
CREATE INDEX IF NOT EXISTS idx_traces_tenant_id ON traces (tenant_id);
CREATE INDEX IF NOT EXISTS idx_traces_workspace_id ON traces (workspace_id);

CREATE INDEX IF NOT EXISTS idx_user_input_requests_request_id ON user_input_requests (request_id);
CREATE INDEX IF NOT EXISTS idx_user_input_requests_run_id ON user_input_requests (run_id);
CREATE INDEX IF NOT EXISTS idx_user_input_requests_agent_id ON user_input_requests (agent_id);

CREATE INDEX IF NOT EXISTS idx_vector_store_config_tenant_id ON vector_store_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_vector_store_config_workspace_id ON vector_store_config (workspace_id);

CREATE INDEX IF NOT EXISTS idx_workflow_stage_workflow_id ON workflow_stage (workflow_id);

CREATE INDEX IF NOT EXISTS idx_workspace_tenant_id ON workspace (tenant_id);

