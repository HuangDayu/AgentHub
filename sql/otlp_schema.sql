-- =========================================================
-- AgentHub - OTLP Tables Schema
-- OpenTelemetry Protocol Tables for storing traces, metrics, and logs
-- =========================================================

SET search_path TO app, public;

-- =========================================================
-- OTLP Tables
-- =========================================================

-- Table: otlp_span (存储追踪数据)
CREATE TABLE IF NOT EXISTS otlp_span
(
    id                 varchar(64) NOT NULL,
    span_id            varchar(64),
    trace_id           varchar(64),
    parent_span_id     varchar(64),
    operation_name     varchar(512),
    service_name       varchar(255),
    kind               varchar(32),
    start_timestamp    bigint,
    end_timestamp      bigint,
    duration           bigint,
    status             varchar(32),
    status_description text,
    attributes         text,
    events             text,
    links              text,
    tenant_id          varchar(255),
    workspace_id       varchar(255),
    created_at         timestamptz,
    PRIMARY KEY (id)
);

-- Table: otlp_metric (存储指标数据)
CREATE TABLE IF NOT EXISTS otlp_metric
(
    id            varchar(64) NOT NULL,
    metric_name   varchar(255),
    description   text,
    unit          varchar(64),
    metric_type   varchar(32),
    service_name  varchar(255),
    value         text,
    attributes    text,
    timestamp     bigint,
    tenant_id     varchar(255),
    workspace_id  varchar(255),
    created_at    timestamptz,
    PRIMARY KEY (id)
);

-- Table: otlp_log (存储日志数据)
CREATE TABLE IF NOT EXISTS otlp_log
(
    id              varchar(64) NOT NULL,
    log_id          varchar(64),
    trace_id        varchar(64),
    span_id         varchar(64),
    service_name    varchar(255),
    severity        varchar(32),
    severity_number integer,
    body            text,
    attributes      text,
    timestamp       bigint,
    tenant_id       varchar(255),
    workspace_id    varchar(255),
    created_at      timestamptz,
    PRIMARY KEY (id)
);

-- =========================================================
-- Indexes for OTLP Tables
-- =========================================================

-- Span indexes
CREATE INDEX IF NOT EXISTS idx_otlp_span_trace_id ON otlp_span (trace_id);
CREATE INDEX IF NOT EXISTS idx_otlp_span_span_id ON otlp_span (span_id);
CREATE INDEX IF NOT EXISTS idx_otlp_span_service_name ON otlp_span (service_name);
CREATE INDEX IF NOT EXISTS idx_otlp_span_operation_name ON otlp_span (operation_name);
CREATE INDEX IF NOT EXISTS idx_otlp_span_start_timestamp ON otlp_span (start_timestamp);
CREATE INDEX IF NOT EXISTS idx_otlp_span_tenant_id ON otlp_span (tenant_id);
CREATE INDEX IF NOT EXISTS idx_otlp_span_workspace_id ON otlp_span (workspace_id);

-- Metric indexes
CREATE INDEX IF NOT EXISTS idx_otlp_metric_metric_name ON otlp_metric (metric_name);
CREATE INDEX IF NOT EXISTS idx_otlp_metric_service_name ON otlp_metric (service_name);
CREATE INDEX IF NOT EXISTS idx_otlp_metric_metric_type ON otlp_metric (metric_type);
CREATE INDEX IF NOT EXISTS idx_otlp_metric_timestamp ON otlp_metric (timestamp);
CREATE INDEX IF NOT EXISTS idx_otlp_metric_tenant_id ON otlp_metric (tenant_id);
CREATE INDEX IF NOT EXISTS idx_otlp_metric_workspace_id ON otlp_metric (workspace_id);

-- Log indexes
CREATE INDEX IF NOT EXISTS idx_otlp_log_trace_id ON otlp_log (trace_id);
CREATE INDEX IF NOT EXISTS idx_otlp_log_span_id ON otlp_log (span_id);
CREATE INDEX IF NOT EXISTS idx_otlp_log_service_name ON otlp_log (service_name);
CREATE INDEX IF NOT EXISTS idx_otlp_log_severity ON otlp_log (severity);
CREATE INDEX IF NOT EXISTS idx_otlp_log_timestamp ON otlp_log (timestamp);
CREATE INDEX IF NOT EXISTS idx_otlp_log_tenant_id ON otlp_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_otlp_log_workspace_id ON otlp_log (workspace_id);
