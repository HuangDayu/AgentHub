-- =========================================================
-- AgentHub Test Data - Seed Data
-- Purpose: Initialize admin, tenants, users, and role bindings
-- Passwords: admin=admin123, others=user123 (BCrypt)
-- Idempotent: Safe to run multiple times
-- =========================================================

-- BCrypt hashes:
-- admin123 => $2b$10$Qz1RAwD5g0.Ctm8Fz9P/0eJvawm93Cvm3lgt6WFI0/SLvkatA7vNC
-- user123  => $2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG

-- =========================================================
-- 1. System Roles
-- =========================================================
INSERT INTO role_def (id, role_code, role_name, role_type)
VALUES ('r-owner', 'ROLE_OWNER', 'Owner', 'SYSTEM'),
       ('r-admin', 'ROLE_ADMIN', 'Admin', 'SYSTEM'),
       ('r-user', 'ROLE_USER', 'User', 'SYSTEM'),
       ('r-auditor', 'ROLE_AUDITOR', 'Auditor', 'SYSTEM')
ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- 2. Tenants
-- =========================================================
INSERT INTO tenant (id, tenant_code, name, plan_code, isolation_level, status, region, created_at, updated_at)
VALUES ('100000001', 'platform', 'Platform', 'ENTERPRISE', 'L3', 'ACTIVE', 'cn-east-1', '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'),
       ('100000002', 'acme', 'Acme Corporation', 'PRO', 'L1', 'ACTIVE', 'cn-east-1', '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z'),
       ('100000003', 'globex', 'Globex Industries', 'PRO', 'L1', 'ACTIVE', 'cn-east-1', '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- 3. Workspaces
-- =========================================================
INSERT INTO workspace (id, tenant_id, workspace_code, name, region, status, created_at)
VALUES ('100000001', '100000001', 'default', 'Platform Workspace', 'cn-east-1', 'ACTIVE', '2026-01-01T00:00:00Z'),
       ('100000002', '100000002', 'default', 'Acme Workspace', 'cn-east-1', 'ACTIVE', '2026-01-01T00:00:00Z'),
       ('100000003', '100000003', 'default', 'Globex Workspace', 'cn-east-1', 'ACTIVE', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- 4. Platform Users (password: admin123)
-- =========================================================
INSERT INTO app_user (id, tenant_id, username, email, display_name, status, auth_source, password_hash)
VALUES ('100000001', '100000001', 'admin', 'admin@platform.local', 'Platform Admin', 'ACTIVE', 'LOCAL',
        '$2b$10$Qz1RAwD5g0.Ctm8Fz9P/0eJvawm93Cvm3lgt6WFI0/SLvkatA7vNC')
ON CONFLICT (id) DO UPDATE SET status = 'ACTIVE';

-- =========================================================
-- 5. Acme Users (password: user123 for all)
-- =========================================================
INSERT INTO app_user (id, tenant_id, username, email, display_name, status, auth_source, password_hash)
VALUES ('000000000010', '100000002', 'zhangsan', 'zhangsan@acme.local', '张三', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000011', '100000002', 'lisi', 'lisi@acme.local', '李四', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000012', '100000002', 'wangwu', 'wangwu@acme.local', '王五', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000013', '100000002', 'zhaoliu', 'zhaoliu@acme.local', '赵六', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000014', '100000002', 'sunqi', 'sunqi@acme.local', '孙七', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000015', '100000002', 'zhouba', 'zhouba@acme.local', '周八', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000016', '100000002', 'zhengjiu', 'zhengjiu@acme.local', '郑九', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000017', '100000002', 'shishi', 'shishi@acme.local', '石十', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG')
ON CONFLICT (id) DO UPDATE SET status = 'ACTIVE';

-- =========================================================
-- 6. Globex Users (password: user123 for all)
-- =========================================================
INSERT INTO app_user (id, tenant_id, username, email, display_name, status, auth_source, password_hash)
VALUES ('000000000020', '100000003', 'alice', 'alice@globex.local', 'Alice', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000021', '100000003', 'bob', 'bob@globex.local', 'Bob', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000022', '100000003', 'charlie', 'charlie@globex.local', 'Charlie', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000023', '100000003', 'david', 'david@globex.local', 'David', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000024', '100000003', 'eve', 'eve@globex.local', 'Eve', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000025', '100000003', 'frank', 'frank@globex.local', 'Frank', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000026', '100000003', 'grace', 'grace@globex.local', 'Grace', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG'),
       ('000000000027', '100000003', 'henry', 'henry@globex.local', 'Henry', 'ACTIVE', 'LOCAL',
        '$2b$10$jBS7r8PAF.UghC7iFA/FJuIv2e3c6EAwHesnflZQR5ZMgMmxZ1fPG')
ON CONFLICT (id) DO UPDATE SET status = 'ACTIVE';

-- =========================================================
-- 7. Role Bindings
-- =========================================================

-- Platform: admin - OWNER (TENANT)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-platform-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000001'
  AND u.username = 'admin'
  AND d.role_code = 'ROLE_OWNER'
ON CONFLICT (id) DO NOTHING;

-- Acme: zhangsan - OWNER (TENANT)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-acme-owner-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000002'
  AND u.username = 'zhangsan'
  AND d.role_code = 'ROLE_OWNER'
ON CONFLICT (id) DO NOTHING;

-- Acme: lisi - ADMIN (TENANT)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-acme-admin-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000002'
  AND u.username = 'lisi'
  AND d.role_code = 'ROLE_ADMIN'
ON CONFLICT (id) DO NOTHING;

-- Acme: wangwu~shishi - USER (WORKSPACE)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-acme-user-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000002'
  AND u.username IN ('wangwu', 'zhaoliu', 'sunqi', 'zhouba', 'zhengjiu', 'shishi')
  AND d.role_code = 'ROLE_USER'
ON CONFLICT (id) DO NOTHING;

-- Globex: alice - OWNER (TENANT)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-globex-owner-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000003'
  AND u.username = 'alice'
  AND d.role_code = 'ROLE_OWNER'
ON CONFLICT (id) DO NOTHING;

-- Globex: bob - ADMIN (TENANT)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-globex-admin-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000003'
  AND u.username = 'bob'
  AND d.role_code = 'ROLE_ADMIN'
ON CONFLICT (id) DO NOTHING;

-- Globex: charlie~henry - USER (WORKSPACE)
INSERT INTO role_binding (id, user_id, role_id, created_at)
SELECT CONCAT('rb-globex-user-', gen_random_uuid()), u.id, d.id, '2026-01-01T00:00:00Z'
FROM app_user u,
     role_def d
WHERE u.tenant_id = '100000003'
  AND u.username IN ('charlie', 'david', 'eve', 'frank', 'grace', 'henry')
  AND d.role_code = 'ROLE_USER'
ON CONFLICT (id) DO NOTHING;

-- =========================================================
-- 8. Test Data for Integration Tests
-- =========================================================

-- Policy and Policy Rule for governance-domain tests
INSERT INTO policy (id, effect, conditions, created_at, updated_at)
VALUES ('test-policy-allow', 'ALLOW', '{"role": "admin"}', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO UPDATE SET effect     = 'ALLOW',
                               conditions = '{"role": "admin"}';

INSERT INTO policy_rule (id, tenant_id, scope_type, name, description, effect, action_pattern, resource_pattern,
                             condition_expr, enabled, created_at, updated_at)
VALUES ('test-rule-1', '100000002', 'GLOBAL', 'Test Rule', 'Test policy rule for integration tests', 'ALLOW', '*', '*',
        '{}', true, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO UPDATE SET name    = 'Test Rule',
                               effect  = 'ALLOW',
                               enabled = true;

-- Billing data for billing-domain tests
-- INSERT INTO usage_meter (id, tenant_id, workspace_id, metric_type, quantity, unit_price, amount, source_service, source_ref_id, recorded_at, created_at)
-- VALUES ('usage-001', '100000002', '100000002', 'TOKENS', 1000000, 0.0001, 100.00, 'knowledge-service', 'ref-001', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
-- ON CONFLICT (id) DO UPDATE SET quantity = 1000000, amount = 100.00;

-- INSERT INTO billing_invoice (id, tenant_id, period_month, status, subtotal_amount, tax_amount, total_amount, currency, invoice_payload, created_at, updated_at)
-- VALUES ('inv-001', '100000002', '2026-01', 'PAID', 100.00, 10.00, 110.00, 'CNY', '{"items": []}', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
-- ON CONFLICT (id) DO UPDATE SET status = 'PAID', total_amount = 110.00;

-- Vector Store Config for agenthub tests
INSERT INTO vector_store_config (id, tenant_id, workspace_id, name, type, host, port, api_key, collection_name,
                                     created_at, updated_at)
VALUES ('vsc-chroma-test', '100000002', '100000002', 'Chroma Init Test', 'CHROMA', 'localhost', 8000, null,
        'test-collection', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO UPDATE SET name = 'Chroma Init Test',
                               type = 'CHROMA';

-- Knowledge Base for agenthub tests (Note: This might cause conflict, so we use DO NOTHING)
INSERT INTO knowledge_base (id, tenant_id, workspace_id, kb_code, name, description, status, created_at, created_by,
                                updated_at)
VALUES ('kb-test-001', '100000002', '100000002', 'kb-init-test', 'Create Test KB', 'Test knowledge base', 'ACTIVE',
        '2026-01-01T00:00:00Z', 'lisi', '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Workspace and Workspace Member for tenant-domain tests
INSERT INTO workspace (id, tenant_id, workspace_code, name, status, created_at, updated_at)
VALUES ('100000002', '100000002', 'ws-acme-dev', 'Acme Development', 'ACTIVE', '2026-01-01T00:00:00Z',
        '2026-01-01T00:00:00Z')
ON CONFLICT (id) DO UPDATE SET name   = 'Acme Development',
                               status = 'ACTIVE';

-- INSERT INTO workspace_member (id, workspace_id, user_id, role, joined_at, created_at)
-- SELECT 'wm-001', '100000002', u.id, 'MEMBER', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'
-- FROM app_user u
-- WHERE u.tenant_id = '100000002' AND u.username = 'lisi'
-- ON CONFLICT (id) DO NOTHING;
--
-- -- Notification for tenant-domain tests
-- INSERT INTO notification (id, tenant_id, user_id, type, title, content, status, created_at)
-- SELECT 'notif-001', '100000002', u.id, 'SYSTEM', 'Test Notification', 'This is a test notification', 'UNREAD', '2026-01-01T00:00:00Z'
-- FROM app_user u
-- WHERE u.tenant_id = '100000002' AND u.username = 'lisi'
-- ON CONFLICT (id) DO NOTHING;
