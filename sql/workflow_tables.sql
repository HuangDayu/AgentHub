-- 工作流执行记录表
CREATE TABLE IF NOT EXISTS workflow_execution (
    id VARCHAR(64) PRIMARY KEY,
    workflow_id VARCHAR(64) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    workspace_id VARCHAR(64) NOT NULL,
    execution_id VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    input TEXT,
    output TEXT,
    error_info TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workflow_execution_workflow ON workflow_execution(workflow_id);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_status ON workflow_execution(status);
CREATE INDEX IF NOT EXISTS idx_workflow_execution_time ON workflow_execution(start_time);

-- 节点执行结果表
CREATE TABLE IF NOT EXISTS node_execution_result (
    id VARCHAR(64) PRIMARY KEY,
    execution_id VARCHAR(64) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    node_name VARCHAR(256),
    node_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input TEXT,
    output TEXT,
    error_info TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (execution_id) REFERENCES workflow_execution(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_node_result_execution ON node_execution_result(execution_id);
CREATE INDEX IF NOT EXISTS idx_node_result_node ON node_execution_result(node_id);
CREATE INDEX IF NOT EXISTS idx_node_result_status ON node_execution_result(status);
