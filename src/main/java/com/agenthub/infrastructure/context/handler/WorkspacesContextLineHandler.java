package com.agenthub.infrastructure.context.handler;

import com.agenthub.infrastructure.context.TenantContextGetter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 租户上下文行处理器。
 * <p>
 * MyBatis-Plus租户隔离拦截器处理器。
 * </p>
 */
public class WorkspacesContextLineHandler extends BaseContextLineHandler {


    public WorkspacesContextLineHandler(TenantContextGetter tenantContextGetter) {
        super(tenantContextGetter);
    }


    @Override
    public Expression getTenantId() {
        return new StringValue(tenantContextGetter.getWorkspaceId());
    }


    @Override
    public String getTenantIdColumn() {
        return "workspace_id";
    }


}
