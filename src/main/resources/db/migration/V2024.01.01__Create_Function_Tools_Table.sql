-- Function Tools 管理表
CREATE TABLE IF NOT EXISTS app.function_tools (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64),
    tool_class_name VARCHAR(255) NOT NULL UNIQUE,
    tool_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    method_count INT DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    system_tool BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_function_tools_tenant ON app.function_tools(tenant_id);
CREATE INDEX IF NOT EXISTS idx_function_tools_category ON app.function_tools(category);
CREATE INDEX IF NOT EXISTS idx_function_tools_enabled ON app.function_tools(enabled);

COMMENT ON TABLE app.function_tools IS 'Function工具管理表';
COMMENT ON COLUMN app.function_tools.id IS '主键ID';
COMMENT ON COLUMN app.function_tools.tenant_id IS '租户ID';
COMMENT ON COLUMN app.function_tools.tool_class_name IS '工具类全限定名';
COMMENT ON COLUMN app.function_tools.tool_name IS '工具名称';
COMMENT ON COLUMN app.function_tools.description IS '工具描述';
COMMENT ON COLUMN app.function_tools.category IS '工具分类';
COMMENT ON COLUMN app.function_tools.method_count IS '工具方法数量';
COMMENT ON COLUMN app.function_tools.enabled IS '是否启用';
COMMENT ON COLUMN app.function_tools.system_tool IS '是否系统工具';
COMMENT ON COLUMN app.function_tools.created_at IS '创建时间';
COMMENT ON COLUMN app.function_tools.updated_at IS '更新时间';
