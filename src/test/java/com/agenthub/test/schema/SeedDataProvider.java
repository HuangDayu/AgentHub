package com.agenthub.test.schema;

import java.util.List;

/**
 * 测试种子数据：3 租户 + 3 工作空间 + admin 用户。
 * 精简到集成测试真正使用的 ID（100000001/100000002/100000003）。
 */
public final class SeedDataProvider {

    public static final String ADMIN_PASSWORD_HASH = "$2b$10$Qz1RAwD5g0.Ctm8Fz9P/0eJvawm93Cvm3lgt6WFI0/SLvkatA7vNC";
    public static final String USER_PASSWORD_HASH = "$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG";
    public static final String TS = "2026-01-01T00:00:00Z";

    private SeedDataProvider() {
    }

    /**
     * 返回所有种子行
     */
    public static List<SeedRow> all() {
        return List.of(
                tenants(),
                workspaces(),
                adminUser(),
                lisiUser(),
                zhangsanUser()
        );
    }

    /**
     * 3 个租户
     */
    public static SeedRow tenants() {
        return new SeedRow("tenant",
                List.of("id", "tenant_code", "name", "plan_code", "isolation_level", "status", "region", "created_at", "updated_at"),
                List.of(
                        "'100000001', 'platform', 'Platform',         'ENTERPRISE', 'L3', 'ACTIVE', 'cn-east-1', '" + TS + "', '" + TS + "'",
                        "'100000002', 'acme',     'Acme Corporation', 'PRO',        'L1', 'ACTIVE', 'cn-east-1', '" + TS + "', '" + TS + "'",
                        "'100000003', 'globex',   'Globex Industries','PRO',        'L1', 'ACTIVE', 'cn-east-1', '" + TS + "', '" + TS + "'"
                ),
                ConflictMode.DO_NOTHING);
    }

    /**
     * 3 个默认工作空间
     */
    public static SeedRow workspaces() {
        return new SeedRow("workspace",
                List.of("id", "tenant_id", "workspace_code", "name", "region", "status", "created_at", "updated_at"),
                List.of(
                        "'100000001', '100000001', 'default', 'Platform Workspace', 'cn-east-1', 'ACTIVE', '" + TS + "', '" + TS + "'",
                        "'100000002', '100000002', 'default', 'Acme Workspace',     'cn-east-1', 'ACTIVE', '" + TS + "', '" + TS + "'",
                        "'100000003', '100000003', 'default', 'Globex Workspace',   'cn-east-1', 'ACTIVE', '" + TS + "', '" + TS + "'"
                ),
                ConflictMode.DO_NOTHING);
    }

    /**
     * admin 用户（密码 admin123）
     */
    public static SeedRow adminUser() {
        return new SeedRow("app_user",
                List.of("id", "tenant_id", "username", "email", "display_name", "status", "auth_source", "password_hash",
                        "created_at", "updated_at"),
                List.of(
                        "'100000001', '100000001', 'admin', 'admin@platform.local', 'Platform Admin', 'ACTIVE', 'LOCAL', '" + ADMIN_PASSWORD_HASH + "', '" + TS + "', '" + TS + "'"
                ),
                ConflictMode.DO_UPDATE);
    }

    public static SeedRow lisiUser() {
        return new SeedRow("app_user",
                List.of("id", "tenant_id", "username", "email", "display_name", "status", "auth_source", "password_hash",
                        "created_at", "updated_at"),
                List.of(
                        "'000000000011', '100000002', 'lisi', 'lisi@acme.local', '李四', 'ACTIVE', 'LOCAL', '" + USER_PASSWORD_HASH + "', '" + TS + "', '" + TS + "'"
                ),
                ConflictMode.DO_UPDATE);
    }


    public static SeedRow zhangsanUser() {
        return new SeedRow("app_user",
                List.of("id", "tenant_id", "username", "email", "display_name", "status", "auth_source", "password_hash",
                        "created_at", "updated_at"),
                List.of(
                        "'000000000010', '100000003', 'zhangsan', 'lisi@acme.local', '张三', 'ACTIVE', 'LOCAL', '" + USER_PASSWORD_HASH + "', '" + TS + "', '" + TS + "'"
                ),
                ConflictMode.DO_UPDATE);
    }

    /**
     * 种子行：表名 + 列 + 多行 VALUES + 冲突策略
     */
    public record SeedRow(String table, List<String> columns, List<String> values, ConflictMode mode) {
    }

    /**
     * 冲突策略：DO_NOTHING（跳过）/ DO_UPDATE（更新指定列）
     */
    public enum ConflictMode {
        DO_NOTHING, DO_UPDATE
    }
}
