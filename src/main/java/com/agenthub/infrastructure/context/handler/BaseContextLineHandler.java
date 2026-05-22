package com.agenthub.infrastructure.context.handler;

import com.agenthub.infrastructure.context.TenantContextGetter;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
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
public abstract class BaseContextLineHandler implements TenantLineHandler {
    private static final String TABLE_START = "";
    private final Map<String, Boolean> MAP = new ConcurrentHashMap<>();

    /**
     * 租户上下文获取器
     */
    protected final TenantContextGetter tenantContextGetter;

    /**
     * 构造函数。
     *
     * @param tenantContextGetter 租户上下文获取器
     */
    public BaseContextLineHandler(TenantContextGetter tenantContextGetter) {
        this.tenantContextGetter = tenantContextGetter;
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
        return ignoreContext(tableName, getTenantIdColumn()) || MAP.computeIfAbsent(tableName, k -> ignoreTable(tableName, getTenantIdColumn()));
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

    /**
     * 如果获取上下文异常且表中没有该字段，则忽略表
     *
     * @param tableName
     * @param columnName
     * @return
     */
    private boolean ignoreContext(String tableName, String columnName) {
        try {
            if (tenantContextGetter.isIgnoreTenantContext()) {
                return true;
            }
        } catch (Exception e) {
            return ignoreTable(tableName, columnName);
        }
        return false;
    }
}
