-- =========================================================
-- AgentHub - Auto-generated Schema
-- Generated: 2026-05-15T07:24:12.872574900Z
-- Source: MyBatis-Plus Entity Classes
-- =========================================================

CREATE SCHEMA IF NOT EXISTS app;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
SET search_path TO app, public;

-- =========================================================
-- Drop existing tables (dependency order)
-- =========================================================
-- DROP TABLE IF EXISTS workspace CASCADE;
-- DROP TABLE IF EXISTS workflow CASCADE;
-- DROP TABLE IF EXISTS vector_store_config CASCADE;
-- DROP TABLE IF EXISTS tool_policy_binding CASCADE;
-- DROP TABLE IF EXISTS tool_policy CASCADE;
-- DROP TABLE IF EXISTS tenant CASCADE;
-- DROP TABLE IF EXISTS system_tools CASCADE;
-- DROP TABLE IF EXISTS skill CASCADE;
-- DROP TABLE IF EXISTS session CASCADE;
-- DROP TABLE IF EXISTS scheduled_task CASCADE;
-- DROP TABLE IF EXISTS retrieval_policy CASCADE;
-- DROP TABLE IF EXISTS prompt_template CASCADE;
-- DROP TABLE IF EXISTS model_policy CASCADE;
-- DROP TABLE IF EXISTS model_config CASCADE;
-- DROP TABLE IF EXISTS memory CASCADE;
-- DROP TABLE IF EXISTS mcp_tool CASCADE;
-- DROP TABLE IF EXISTS knowledge_base CASCADE;
-- DROP TABLE IF EXISTS ingestion_job CASCADE;
-- DROP TABLE IF EXISTS ingestion_document CASCADE;
-- DROP TABLE IF EXISTS iam_refresh_token_session CASCADE;
-- DROP TABLE IF EXISTS http_tools CASCADE;
-- DROP TABLE IF EXISTS guardrail_policy CASCADE;
-- DROP TABLE IF EXISTS document_chunk CASCADE;
-- DROP TABLE IF EXISTS chat_message CASCADE;
-- DROP TABLE IF EXISTS app_user CASCADE;
-- DROP TABLE IF EXISTS agent_team CASCADE;
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
    is_active    boolean,
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
    skill_path       varchar(255),
    skill_files_tree varchar(255),
    enabled          boolean,
    created_at       timestamptz,
    updated_at       timestamptz,
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

-- Table: workflow
CREATE TABLE IF NOT EXISTS workflow
(
    id               varchar(64) NOT NULL,
    tenant_id        varchar(255),
    workspace_id     varchar(255),
    workflow_code    varchar(255),
    name             varchar(255),
    description      text,
    graph_definition varchar(255),
    status           varchar(255),
    created_at       timestamptz,
    updated_at       timestamptz,
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

CREATE INDEX IF NOT EXISTS idx_agent_team_tenant_id ON agent_team (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agent_team_workspace_id ON agent_team (workspace_id);

CREATE INDEX IF NOT EXISTS idx_app_user_tenant_id ON app_user (tenant_id);

CREATE INDEX IF NOT EXISTS idx_chat_message_session_id ON chat_message (session_id);

CREATE INDEX IF NOT EXISTS idx_document_chunk_chunk_id ON document_chunk (chunk_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_document_id ON document_chunk (document_id);
CREATE INDEX IF NOT EXISTS idx_document_chunk_kb_id ON document_chunk (kb_id);

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

CREATE INDEX IF NOT EXISTS idx_model_config_tenant_id ON model_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_model_config_workspace_id ON model_config (workspace_id);

CREATE INDEX IF NOT EXISTS idx_model_policy_tenant_id ON model_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_model_policy_workspace_id ON model_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_prompt_template_tenant_id ON prompt_template (tenant_id);
CREATE INDEX IF NOT EXISTS idx_prompt_template_workspace_id ON prompt_template (workspace_id);

CREATE INDEX IF NOT EXISTS idx_retrieval_policy_tenant_id ON retrieval_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_retrieval_policy_workspace_id ON retrieval_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_scheduled_task_tenant_id ON scheduled_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_task_workspace_id ON scheduled_task (workspace_id);

CREATE INDEX IF NOT EXISTS idx_session_agent_id ON session (agent_id);
CREATE INDEX IF NOT EXISTS idx_session_tenant_id ON session (tenant_id);
CREATE INDEX IF NOT EXISTS idx_session_workspace_id ON session (workspace_id);

CREATE INDEX IF NOT EXISTS idx_skill_tenant_id ON skill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_skill_workspace_id ON skill (workspace_id);

CREATE INDEX IF NOT EXISTS idx_system_tools_tenant_id ON system_tools (tenant_id);
CREATE INDEX IF NOT EXISTS idx_system_tools_workspace_id ON system_tools (workspace_id);

CREATE INDEX IF NOT EXISTS idx_tool_policy_tenant_id ON tool_policy (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tool_policy_workspace_id ON tool_policy (workspace_id);

CREATE INDEX IF NOT EXISTS idx_tool_policy_binding_tool_policy_id ON tool_policy_binding (tool_policy_id);
CREATE INDEX IF NOT EXISTS idx_tool_policy_binding_tool_id ON tool_policy_binding (tool_id);

CREATE INDEX IF NOT EXISTS idx_vector_store_config_tenant_id ON vector_store_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_vector_store_config_workspace_id ON vector_store_config (workspace_id);

CREATE INDEX IF NOT EXISTS idx_workflow_tenant_id ON workflow (tenant_id);
CREATE INDEX IF NOT EXISTS idx_workflow_workspace_id ON workflow (workspace_id);

CREATE INDEX IF NOT EXISTS idx_workspace_tenant_id ON workspace (tenant_id);

