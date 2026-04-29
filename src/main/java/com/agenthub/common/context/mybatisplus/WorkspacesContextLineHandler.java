package com.agenthub.common.context.mybatisplus;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.agenthub.common.context.TenantContextGetter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 租户上下文行处理器。
 * <p>
 * MyBatis-Plus租户隔离拦截器处理器。
 * </p>
 */
public class WorkspacesContextLineHandler implements TenantLineHandler {

    private static final String TABLE_START = "app.";
    private static final Map<String, Boolean> MAP = new ConcurrentHashMap<>();

    /**
     * 租户上下文获取器
     */
    private final TenantContextGetter tenantContextGetter;

    /**
     * 构造函数。
     *
     * @param tenantContextGetter 租户上下文获取器
     */
    public WorkspacesContextLineHandler(TenantContextGetter tenantContextGetter) {
        this.tenantContextGetter = tenantContextGetter;
    }

    /**
     * 获取租户ID表达式。
     *
     * @return 租户ID的SQL表达式
     */
    @Override
    public Expression getTenantId() {
        return new StringValue(tenantContextGetter.getWorkspaceId());
    }

    /**
     * 获取租户ID列名。
     *
     * @return 列名
     */
    @Override
    public String getTenantIdColumn() {
        return "workspace_id";
    }

    /**
     * 判断是否忽略表。
     * <p>
     * 根据表是否包含租户ID列决定是否忽略。
     * </p>
     *
     * @param tableName 表名
     * @return 是否忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return MAP.computeIfAbsent(tableName, k -> ignoreTable(tableName, getTenantIdColumn()));
    }


    /**
     * 判断表信息是否包含指定列。
     */
    private boolean ignoreTable(String tableName, String columnName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(TABLE_START + tableName);
        if (tableInfo == null) {
            return false;
        }
        return tableInfo.getFieldList().stream()
                .noneMatch(field -> field.getColumn().equalsIgnoreCase(columnName));
    }
}
