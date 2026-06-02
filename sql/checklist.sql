-- =========================================================
-- Schema Change Checklist
-- Generated: 2026-06-02T08:47:53.160854500Z
-- Description: Incremental changes from previous schema
-- =========================================================

BEGIN;

-- New table: agent
CREATE TABLE IF NOT EXISTS agent (
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
-- New table: workspace
CREATE TABLE IF NOT EXISTS workspace (
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
-- New table: user_input_requests
CREATE TABLE IF NOT EXISTS user_input_requests (
  id varchar(64) NOT NULL,
  request_id varchar(255),
  run_id varchar(255),
  agent_id varchar(255),
  agent_name varchar(255),
  structured_input varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: retrieval_policy
CREATE TABLE IF NOT EXISTS retrieval_policy (
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
-- New table: document_chunk
CREATE TABLE IF NOT EXISTS document_chunk (
  id varchar(64) NOT NULL,
  chunk_id varchar(255),
  document_id varchar(255),
  kb_id varchar(255),
  chunk_index integer,
  token_count integer,
  PRIMARY KEY (id)
);
-- New table: message_pushes
CREATE TABLE IF NOT EXISTS message_pushes (
  id varchar(64) NOT NULL,
  message_id varchar(255),
  run_id varchar(255),
  role varchar(255),
  content text,
  metadata text,
  timestamp timestamptz,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: subsession
CREATE TABLE IF NOT EXISTS subsession (
  id varchar(64) NOT NULL,
  parent_session_id varchar(255),
  subagent_id varchar(255),
  name varchar(255),
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: tenant
CREATE TABLE IF NOT EXISTS tenant (
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
-- New table: agent_execution_plan
CREATE TABLE IF NOT EXISTS agent_execution_plan (
  id varchar(64) NOT NULL,
  agent_id varchar(255),
  session_id varchar(255),
  goal varchar(255),
  status varchar(255),
  current_step_index integer,
  result varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: traces
CREATE TABLE IF NOT EXISTS traces (
  id varchar(64) NOT NULL,
  trace_id varchar(255),
  run_id varchar(255),
  root_span_id varchar(255),
  span_count integer,
  start_time_unix_nano varchar(255),
  end_time_unix_nano varchar(255),
  duration_ns bigint,
  status_code integer,
  error_message varchar(255),
  total_tokens bigint,
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: http_tools
CREATE TABLE IF NOT EXISTS http_tools (
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
-- New table: iam_refresh_token_session
CREATE TABLE IF NOT EXISTS iam_refresh_token_session (
  token varchar(255) NOT NULL,
  subject varchar(255),
  expires_at timestamptz,
  PRIMARY KEY (token)
);
-- New table: alerts
CREATE TABLE IF NOT EXISTS alerts (
  id varchar(64) NOT NULL,
  alert_level varchar(255),
  alert_type varchar(255),
  title varchar(255),
  message varchar(255),
  run_id varchar(255),
  agent_id varchar(255),
  trace_id varchar(255),
  metadata text,
  resolved boolean,
  resolved_at timestamptz,
  resolved_by varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: dag_workflow_execution
CREATE TABLE IF NOT EXISTS dag_workflow_execution (
  id varchar(64) NOT NULL,
  workflow_id varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  execution_id varchar(255),
  status varchar(255),
  input varchar(255),
  output varchar(255),
  error_info varchar(255),
  start_time timestamptz,
  end_time timestamptz,
  duration bigint,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: model_config
CREATE TABLE IF NOT EXISTS model_config (
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
-- New table: app_user
CREATE TABLE IF NOT EXISTS app_user (
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
-- New table: tool_policy
CREATE TABLE IF NOT EXISTS tool_policy (
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
-- New table: metrics
CREATE TABLE IF NOT EXISTS metrics (
  id varchar(64) NOT NULL,
  metric_type varchar(255),
  metric_name varchar(255),
  metric_value double precision,
  run_id varchar(255),
  agent_id varchar(255),
  trace_id varchar(255),
  span_id varchar(255),
  labels text,
  timestamp timestamptz,
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: ingestion_document
CREATE TABLE IF NOT EXISTS ingestion_document (
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
-- New table: system_tools
CREATE TABLE IF NOT EXISTS system_tools (
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
-- New table: memory
CREATE TABLE IF NOT EXISTS memory (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  agent_id varchar(255),
  name varchar(255),
  memory_type varchar(255),
  content text,
  metadata varchar(255),
  importance double precision,
  expires_at timestamptz,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: scheduled_task
CREATE TABLE IF NOT EXISTS scheduled_task (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  task_code varchar(255),
  name varchar(255),
  description text,
  task_type varchar(255),
  cron_expression varchar(255),
  executor_config varchar(255),
  prompt varchar(255),
  enabled boolean,
  last_execute_time timestamptz,
  next_execute_time timestamptz,
  status varchar(255),
  agent_id varchar(255),
  last_run_result text,
  run_count integer,
  created_at timestamptz,
  updated_at timestamptz,
  created_by varchar(255),
  updated_by varchar(255),
  PRIMARY KEY (id)
);
-- New table: dynamic_workflow
CREATE TABLE IF NOT EXISTS dynamic_workflow (
  id varchar(64) NOT NULL,
  agent_id varchar(255),
  session_id varchar(255),
  task varchar(255),
  pattern varchar(255),
  status varchar(255),
  result varchar(255),
  max_concurrent_agents integer,
  total_tokens_used integer,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: session
CREATE TABLE IF NOT EXISTS session (
  id varchar(64) NOT NULL,
  agent_id varchar(255),
  name varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: ingestion_job
CREATE TABLE IF NOT EXISTS ingestion_job (
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
-- New table: chat_message
CREATE TABLE IF NOT EXISTS chat_message (
  id varchar(64) NOT NULL,
  session_id varchar(255),
  role varchar(255),
  content text,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: kv_set
CREATE TABLE IF NOT EXISTS kv_set (
  kv_key varchar(255) NOT NULL,
  member varchar(255),
  PRIMARY KEY (kv_key)
);
-- New table: dag_workflow
CREATE TABLE IF NOT EXISTS dag_workflow (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  workflow_code varchar(255),
  name varchar(255),
  description text,
  graph_definition text,
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: agent_config
CREATE TABLE IF NOT EXISTS agent_config (
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
-- New table: model_policy
CREATE TABLE IF NOT EXISTS model_policy (
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
-- New table: agent_team
CREATE TABLE IF NOT EXISTS agent_team (
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
-- New table: skill
CREATE TABLE IF NOT EXISTS skill (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  skill_code varchar(255),
  name varchar(255),
  description text,
  skill_type varchar(255),
  skill_path text,
  skill_files_tree text,
  source varchar(255),
  source_path text,
  zip_storage_path text,
  config_id varchar(255),
  file_count integer,
  total_size bigint,
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  last_sync_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: knowledge_base
CREATE TABLE IF NOT EXISTS knowledge_base (
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
-- New table: prompt_template
CREATE TABLE IF NOT EXISTS prompt_template (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  category varchar(255),
  content text,
  variables varchar(255),
  active boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: kv_hash
CREATE TABLE IF NOT EXISTS kv_hash (
  kv_key varchar(255) NOT NULL,
  field varchar(255),
  kv_value varchar(255),
  PRIMARY KEY (kv_key)
);
-- New table: skill_config
CREATE TABLE IF NOT EXISTS skill_config (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  name varchar(255),
  description text,
  skill_paths text,
  sync_enabled boolean,
  sync_interval integer,
  auto_sync boolean,
  enabled boolean,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: skill_file
CREATE TABLE IF NOT EXISTS skill_file (
  id varchar(64) NOT NULL,
  skill_id varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  file_path varchar(255),
  file_name varchar(255),
  file_ext varchar(255),
  file_size bigint,
  file_type varchar(255),
  encoding varchar(255),
  storage_path varchar(255),
  checksum varchar(255),
  is_directory boolean,
  metadata text,
  version integer,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: spans
CREATE TABLE IF NOT EXISTS spans (
  id varchar(64) NOT NULL,
  span_id varchar(255),
  trace_id varchar(255),
  parent_span_id varchar(255),
  name varchar(255),
  kind varchar(255),
  start_time_unix_nano varchar(255),
  end_time_unix_nano varchar(255),
  latency_ns bigint,
  attributes text,
  events text,
  links text,
  status_code integer,
  status_message varchar(255),
  resource text,
  scope text,
  model varchar(255),
  input_tokens bigint,
  output_tokens bigint,
  total_tokens bigint,
  conversation_id varchar(255),
  operation_name varchar(255),
  service_name varchar(255),
  start_timestamp bigint,
  end_timestamp bigint,
  duration bigint,
  status varchar(255),
  status_description text,
  run_id varchar(255),
  agent_id varchar(255),
  tenant_id varchar(255),
  workspace_id varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: guardrail_policy
CREATE TABLE IF NOT EXISTS guardrail_policy (
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
-- New table: mcp_tool
CREATE TABLE IF NOT EXISTS mcp_tool (
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
-- New table: subagent
CREATE TABLE IF NOT EXISTS subagent (
  id varchar(64) NOT NULL,
  tenant_id varchar(255),
  workspace_id varchar(255),
  parent_agent_id varchar(255),
  name varchar(255),
  description text,
  system_prompt varchar(255),
  model_config_id varchar(255),
  status varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: kv_store
CREATE TABLE IF NOT EXISTS kv_store (
  kv_key varchar(255) NOT NULL,
  kv_value varchar(255),
  kv_type varchar(255),
  expire_time bigint,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (kv_key)
);
-- New table: vector_store_config
CREATE TABLE IF NOT EXISTS vector_store_config (
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
-- New table: agent_plan_step
CREATE TABLE IF NOT EXISTS agent_plan_step (
  id varchar(64) NOT NULL,
  plan_id varchar(255),
  step_order integer,
  description text,
  tool_name varchar(255),
  tool_input varchar(255),
  status varchar(255),
  output varchar(255),
  subagent_id varchar(255),
  subsession_id varchar(255),
  depends_on varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: workflow_stage
CREATE TABLE IF NOT EXISTS workflow_stage (
  id varchar(64) NOT NULL,
  workflow_id varchar(255),
  stage_order integer,
  name varchar(255),
  stage_type varchar(255),
  system_prompt varchar(255),
  task_template varchar(255),
  depends_on varchar(255),
  status varchar(255),
  output varchar(255),
  completed_task_count integer,
  total_task_count integer,
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: kv_list
CREATE TABLE IF NOT EXISTS kv_list (
  kv_key varchar(255) NOT NULL,
  list_index bigint,
  kv_value varchar(255),
  PRIMARY KEY (kv_key)
);
-- New table: kv_zset
CREATE TABLE IF NOT EXISTS kv_zset (
  kv_key varchar(255) NOT NULL,
  member varchar(255),
  score double precision,
  PRIMARY KEY (kv_key)
);
-- New table: node_execution_result
CREATE TABLE IF NOT EXISTS node_execution_result (
  id varchar(64) NOT NULL,
  execution_id varchar(255),
  node_id varchar(255),
  node_name varchar(255),
  node_type varchar(255),
  status varchar(255),
  input varchar(255),
  output varchar(255),
  error_info varchar(255),
  start_time timestamptz,
  end_time timestamptz,
  duration bigint,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: tool_policy_binding
CREATE TABLE IF NOT EXISTS tool_policy_binding (
  id varchar(64) NOT NULL,
  tool_policy_id varchar(255),
  tool_id varchar(255),
  priority integer,
  enabled boolean,
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: run_registrations
CREATE TABLE IF NOT EXISTS run_registrations (
  id varchar(64) NOT NULL,
  project varchar(255),
  name varchar(255),
  timestamp timestamptz,
  pid integer,
  status varchar(255),
  run_dir varchar(255),
  created_at timestamptz,
  PRIMARY KEY (id)
);
-- New table: agent_task
CREATE TABLE IF NOT EXISTS agent_task (
  id varchar(64) NOT NULL,
  stage_id varchar(255),
  workflow_id varchar(255),
  task_description text,
  subagent_id varchar(255),
  subsession_id varchar(255),
  status varchar(255),
  result varchar(255),
  model_config_id varchar(255),
  tool_names varchar(255),
  created_at timestamptz,
  updated_at timestamptz,
  PRIMARY KEY (id)
);
COMMIT;
