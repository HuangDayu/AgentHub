-- =========================================================
-- Schema Change Checklist
-- Generated: 2026-05-07T09:26:56.133624900Z
-- Description: Incremental changes from previous schema
-- =========================================================

BEGIN;

-- New table: app.model_policy
CREATE TABLE IF NOT EXISTS app.model_policy (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  temperature double precision,
  max_tokens integer,
  top_p double precision,
  frequency_penalty double precision,
  presence_penalty double precision,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.retrieval_policy
CREATE TABLE IF NOT EXISTS app.retrieval_policy (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  retrieval_type varchar(255),
  top_k integer,
  score_threshold double precision,
  enable_rerank boolean,
  enable_query_rewrite boolean,
  enable_text_search boolean,
  enable_vector_search boolean,
  rerank_model varchar(255),
  vector_weight double precision,
  keyword_weight double precision,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.memory
CREATE TABLE IF NOT EXISTS app.memory (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  agent_id varchar(255),
  memory_type varchar(255),
  content text,
  metadata varchar(255),
  importance double precision,
  expires_at timestamptz,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.model_config
CREATE TABLE IF NOT EXISTS app.model_config (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  type varchar(255),
  supplier varchar(255),
  api_key varchar(255),
  base_url text,
  model varchar(255),
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  created_by varchar(255),
  PRIMARY KEY (id)
);
-- New table: app.tool_policy
CREATE TABLE IF NOT EXISTS app.tool_policy (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  max_concurrent_calls integer,
  timeout_seconds integer,
  retry_count integer,
  fallback_enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.agent
CREATE TABLE IF NOT EXISTS app.agent (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  agent_code varchar(255),
  name varchar(255),
  description text,
  status varchar(255),
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  created_by varchar(255),
  updated_by varchar(255),
  PRIMARY KEY (id)
);
-- New table: app.session
CREATE TABLE IF NOT EXISTS app.session (
  id varchar(64) NOT NULL,
  agent_id varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.workflow
CREATE TABLE IF NOT EXISTS app.workflow (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  workflow_code varchar(255),
  name varchar(255),
  description text,
  graph_definition varchar(255),
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.tenant
CREATE TABLE IF NOT EXISTS app.tenant (
  id varchar(64) NOT NULL,
  tenant_code varchar(255),
  name varchar(255),
  plan_code varchar(255),
  isolation_level varchar(255),
  status varchar(255),
  region varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.chat_message
CREATE TABLE IF NOT EXISTS app.chat_message (
  id varchar(64) NOT NULL,
  session_id varchar(255),
  role varchar(255),
  content text,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.mcp_tool
CREATE TABLE IF NOT EXISTS app.mcp_tool (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  server_url text,
  server_type varchar(255),
  command varchar(255),
  args varchar(255),
  env varchar(255),
  async boolean,
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.iam_refresh_token_session
CREATE TABLE IF NOT EXISTS app.iam_refresh_token_session (
  token varchar(255) NOT NULL,
  subject varchar(255),
  expires_at timestamptz,
  PRIMARY KEY (token)
);
-- New table: app.knowledge_base
CREATE TABLE IF NOT EXISTS app.knowledge_base (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  kb_code varchar(255),
  name varchar(255),
  description text,
  status varchar(255),
  created_at timestamptz,
  created_by varchar(255),
  updated_at timestamptz,
  updated_by varchar(255),
  vector_store_config_id varchar(255),
  embedding_model_config_id varchar(255),
  chat_model_config_id varchar(255),
  PRIMARY KEY (id)
);
-- New table: app.security_policy
CREATE TABLE IF NOT EXISTS app.security_policy (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  input_validation boolean,
  output_filtering boolean,
  rate_limit_enabled boolean,
  rate_limit_per_minute integer,
  content_moderation boolean,
  pii_detection boolean,
  allowed_domains varchar(255),
  blocked_patterns varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.tool_policy_binding
CREATE TABLE IF NOT EXISTS app.tool_policy_binding (
  id varchar(64) NOT NULL,
  tool_policy_id varchar(255),
  tool_id varchar(255),
  priority integer,
  enabled boolean,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.ingestion_job
CREATE TABLE IF NOT EXISTS app.ingestion_job (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  kb_id varchar(255),
  document_id varchar(255),
  trigger_type varchar(255),
  status varchar(255),
  progress integer,
  parser_name varchar(255),
  embedding_model varchar(255),
  index_version integer,
  document_count integer,
  error_code varchar(255),
  error_message varchar(255),
  started_at timestamptz,
  ended_at timestamptz,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.guardrail_policy
CREATE TABLE IF NOT EXISTS app.guardrail_policy (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  input_validation_enabled boolean,
  output_validation_enabled boolean,
  pii_detection_enabled boolean,
  pii_masking_enabled boolean,
  prompt_injection_detection boolean,
  max_input_length integer,
  max_output_length integer,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.agent_config
CREATE TABLE IF NOT EXISTS app.agent_config (
  id varchar(64) NOT NULL,
  agent_id varchar(255),
  category varchar(255),
  type varchar(255),
  config_id varchar(255),
  name varchar(255),
  description text,
  priority integer,
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.http_tools
CREATE TABLE IF NOT EXISTS app.http_tools (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  enabled boolean,
  endpoint varchar(255),
  auth_type varchar(255),
  input_schema varchar(255),
  timeout_ms integer,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.document_chunk
CREATE TABLE IF NOT EXISTS app.document_chunk (
  id varchar(64) NOT NULL,
  chunk_id varchar(255),
  document_id varchar(255),
  kb_id varchar(255),
  chunk_index integer,
  token_count integer,
  PRIMARY KEY (id)
);
-- New table: app.ingestion_document
CREATE TABLE IF NOT EXISTS app.ingestion_document (
  id varchar(64) NOT NULL,
  kb_id varchar(255),
  job_id varchar(255),
  file_name varchar(255),
  content_type text,
  size bigint,
  storage_path varchar(255),
  status varchar(255),
  PRIMARY KEY (id)
);
-- New table: app.app_user
CREATE TABLE IF NOT EXISTS app.app_user (
  id varchar(64) NOT NULL,
  username varchar(255),
  password_hash varchar(255),
  tenant_id varchar(255),
  status varchar(255),
  email varchar(255),
  display_name varchar(255),
  auth_source varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.agent_team
CREATE TABLE IF NOT EXISTS app.agent_team (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  team_code varchar(255),
  name varchar(255),
  description text,
  coordination_mode varchar(255),
  member_config varchar(255),
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.skill
CREATE TABLE IF NOT EXISTS app.skill (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  skill_code varchar(255),
  name varchar(255),
  description text,
  skill_type varchar(255),
  skill_path varchar(255),
  skill_files_tree varchar(255),
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.system_tools
CREATE TABLE IF NOT EXISTS app.system_tools (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  tool_class_name varchar(255),
  tool_name varchar(255),
  description text,
  category varchar(255),
  method_count integer,
  enabled boolean,
  system_tool boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.vector_store_config
CREATE TABLE IF NOT EXISTS app.vector_store_config (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  type varchar(255),
  host varchar(255),
  port integer,
  api_key varchar(255),
  collection_name varchar(255),
  extra_params varchar(255),
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.prompt_template
CREATE TABLE IF NOT EXISTS app.prompt_template (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  category varchar(255),
  content text,
  variables varchar(255),
  is_active boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: app.workspace
CREATE TABLE IF NOT EXISTS app.workspace (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_code varchar(255),
  name varchar(255),
  region varchar(255),
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
COMMIT;
