-- =========================================================
-- Things Knowledge Platform - Memory, Skill, Workflow, Team, SecurityPolicy Tables
-- Migration: V20260426
-- =========================================================

SET search_path TO app, public;

-- =========================================================
-- Table: app.memory (记忆管理)
-- =========================================================
CREATE TABLE IF NOT EXISTS app.memory
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    agent_id     varchar(255),
    memory_type  varchar(255),
    content      text,
    metadata     text,
    importance   double precision,
    expires_at   timestamptz,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

COMMENT ON TABLE app.memory IS 'Agent记忆存储表';
COMMENT ON COLUMN app.memory.id IS '记忆ID';
COMMENT ON COLUMN app.memory.tenant_id IS '租户ID';
COMMENT ON COLUMN app.memory.workspace_id IS '工作空间ID';
COMMENT ON COLUMN app.memory.agent_id IS 'Agent ID';
COMMENT ON COLUMN app.memory.memory_type IS '记忆类型';
COMMENT ON COLUMN app.memory.content IS '记忆内容';
COMMENT ON COLUMN app.memory.metadata IS '元数据(JSON)';
COMMENT ON COLUMN app.memory.importance IS '重要性分数';
COMMENT ON COLUMN app.memory.expires_at IS '过期时间';

-- Indexes for memory table
CREATE INDEX IF NOT EXISTS idx_memory_tenant_workspace ON app.memory(tenant_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_memory_agent ON app.memory(agent_id);
CREATE INDEX IF NOT EXISTS idx_memory_type ON app.memory(memory_type);

-- =========================================================
-- Table: app.skill (技能管理)
-- =========================================================
CREATE TABLE IF NOT EXISTS app.skill
(
    id           varchar(64) NOT NULL,
    tenant_id    varchar(255),
    workspace_id varchar(255),
    skill_code   varchar(255),
    name         varchar(255),
    description  text,
    skill_type   varchar(255),
    definition   text,
    parameters   text,
    enabled      boolean,
    created_at   timestamptz,
    updated_at   timestamptz,
    PRIMARY KEY (id)
);

COMMENT ON TABLE app.skill IS '技能定义表';
COMMENT ON COLUMN app.skill.id IS '技能ID';
COMMENT ON COLUMN app.skill.tenant_id IS '租户ID';
COMMENT ON COLUMN app.skill.workspace_id IS '工作空间ID';
COMMENT ON COLUMN app.skill.skill_code IS '技能编码';
COMMENT ON COLUMN app.skill.name IS '技能名称';
COMMENT ON COLUMN app.skill.description IS '技能描述';
COMMENT ON COLUMN app.skill.skill_type IS '技能类型';
COMMENT ON COLUMN app.skill.definition IS '技能定义(JSON)';
COMMENT ON COLUMN app.skill.parameters IS '参数定义(JSON)';
COMMENT ON COLUMN app.skill.enabled IS '是否启用';

-- Indexes for skill table
CREATE INDEX IF NOT EXISTS idx_skill_tenant_workspace ON app.skill(tenant_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_skill_code ON app.skill(skill_code);

-- =========================================================
-- Table: app.workflow (工作流图管理)
-- =========================================================
CREATE TABLE IF NOT EXISTS app.workflow
(
    id              varchar(64) NOT NULL,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    workflow_code   varchar(255),
    name            varchar(255),
    description     text,
    graph_definition text,
    status          varchar(255),
    created_at      timestamptz,
    updated_at      timestamptz,
    PRIMARY KEY (id)
);

COMMENT ON TABLE app.workflow IS '工作流图定义表';
COMMENT ON COLUMN app.workflow.id IS '工作流ID';
COMMENT ON COLUMN app.workflow.tenant_id IS '租户ID';
COMMENT ON COLUMN app.workflow.workspace_id IS '工作空间ID';
COMMENT ON COLUMN app.workflow.workflow_code IS '工作流编码';
COMMENT ON COLUMN app.workflow.name IS '工作流名称';
COMMENT ON COLUMN app.workflow.description IS '工作流描述';
COMMENT ON COLUMN app.workflow.graph_definition IS '图定义(JSON/DAG)';
COMMENT ON COLUMN app.workflow.status IS '状态(DRAFT/PUBLISHED)';

-- Indexes for workflow table
CREATE INDEX IF NOT EXISTS idx_workflow_tenant_workspace ON app.workflow(tenant_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_workflow_code ON app.workflow(workflow_code);

-- =========================================================
-- Table: app.agent_team (Agent团队管理)
-- =========================================================
CREATE TABLE IF NOT EXISTS app.agent_team
(
    id               varchar(64) NOT NULL,
    tenant_id        varchar(255),
    workspace_id     varchar(255),
    team_code        varchar(255),
    name             varchar(255),
    description      text,
    coordination_mode varchar(255),
    member_config    text,
    status           varchar(255),
    created_at       timestamptz,
    updated_at       timestamptz,
    PRIMARY KEY (id)
);

COMMENT ON TABLE app.agent_team IS 'Agent团队表';
COMMENT ON COLUMN app.agent_team.id IS '团队ID';
COMMENT ON COLUMN app.agent_team.tenant_id IS '租户ID';
COMMENT ON COLUMN app.agent_team.workspace_id IS '工作空间ID';
COMMENT ON COLUMN app.agent_team.team_code IS '团队编码';
COMMENT ON COLUMN app.agent_team.name IS '团队名称';
COMMENT ON COLUMN app.agent_team.description IS '团队描述';
COMMENT ON COLUMN app.agent_team.coordination_mode IS '协调模式';
COMMENT ON COLUMN app.agent_team.member_config IS '成员配置(JSON)';
COMMENT ON COLUMN app.agent_team.status IS '状态(DRAFT/ACTIVE/INACTIVE)';

-- Indexes for agent_team table
CREATE INDEX IF NOT EXISTS idx_agent_team_tenant_workspace ON app.agent_team(tenant_id, workspace_id);
CREATE INDEX IF NOT EXISTS idx_agent_team_code ON app.agent_team(team_code);

-- =========================================================
-- Table: app.security_policy (安全策略管理)
-- =========================================================
CREATE TABLE IF NOT EXISTS app.security_policy
(
    id                    varchar(64) NOT NULL,
    tenant_id             varchar(255),
    workspace_id          varchar(255),
    name                  varchar(255),
    description           text,
    input_validation      boolean,
    output_filtering      boolean,
    rate_limit_enabled    boolean,
    rate_limit_per_minute integer,
    content_moderation    boolean,
    pii_detection         boolean,
    allowed_domains       text,
    blocked_patterns      text,
    created_at            timestamptz,
    updated_at            timestamptz,
    PRIMARY KEY (id)
);

COMMENT ON TABLE app.security_policy IS '安全策略表';
COMMENT ON COLUMN app.security_policy.id IS '策略ID';
COMMENT ON COLUMN app.security_policy.tenant_id IS '租户ID';
COMMENT ON COLUMN app.security_policy.workspace_id IS '工作空间ID';
COMMENT ON COLUMN app.security_policy.name IS '策略名称';
COMMENT ON COLUMN app.security_policy.description IS '策略描述';
COMMENT ON COLUMN app.security_policy.input_validation IS '输入验证';
COMMENT ON COLUMN app.security_policy.output_filtering IS '输出过滤';
COMMENT ON COLUMN app.security_policy.rate_limit_enabled IS '限流启用';
COMMENT ON COLUMN app.security_policy.rate_limit_per_minute IS '每分钟限流数';
COMMENT ON COLUMN app.security_policy.content_moderation IS '内容审核';
COMMENT ON COLUMN app.security_policy.pii_detection IS 'PII检测';
COMMENT ON COLUMN app.security_policy.allowed_domains IS '允许域名(JSON)';
COMMENT ON COLUMN app.security_policy.blocked_patterns IS '阻止模式(JSON)';

-- Indexes for security_policy table
CREATE INDEX IF NOT EXISTS idx_security_policy_tenant_workspace ON app.security_policy(tenant_id, workspace_id);
