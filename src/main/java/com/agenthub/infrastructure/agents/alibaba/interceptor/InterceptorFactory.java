package com.agenthub.infrastructure.agents.alibaba.interceptor;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import org.springframework.stereotype.Component;

/**
 * 工具拦截器工厂，提供常用拦截器实例。
 */
@Component
public class InterceptorFactory {

    /** 监控拦截器。 */
    public Interceptor monitoringInterceptor() {
        return new ToolMonitoringInterceptor();
    }

    /**
     * 工具策略拦截器。
     *
     * @param context Agent 上下文
     * @return 工具策略拦截器
     */
    public Interceptor toolStrategyInterceptor(ReActAgentContext context) {
        return new ToolStrategyInterceptor(context);
    }
}
