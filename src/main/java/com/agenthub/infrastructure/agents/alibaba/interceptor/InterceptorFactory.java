package com.agenthub.infrastructure.agents.alibaba.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import org.springframework.stereotype.Component;

/**
 * 工具拦截器工厂，提供常用拦截器实例。
 */
@Component
public class InterceptorFactory {

    public Interceptor monitoringInterceptor() {
        return new ToolMonitoringInterceptor();
    }


}
