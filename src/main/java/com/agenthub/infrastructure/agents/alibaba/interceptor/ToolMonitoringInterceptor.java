package com.agenthub.infrastructure.agents.alibaba.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangdayu
 */
public class ToolMonitoringInterceptor extends ToolInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ToolMonitoringInterceptor.class);

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        return doIntercept(request, handler);
    }

    private ToolCallResponse doIntercept(ToolCallRequest request, ToolCallHandler handler) {
        long start = System.currentTimeMillis();
        try {
            ToolCallResponse response = handler.call(request);
            logSuccess(request, start);
            return response;
        } catch (Exception e) {
            return handleError(request, start, e);
        }
    }

    private void logSuccess(ToolCallRequest request, long start) {
        log.info("工具【{}】执行成功，耗时：{}ms", request.getToolName(), System.currentTimeMillis() - start);
    }

    private ToolCallResponse handleError(ToolCallRequest request, long start, Exception e) {
        log.error("工具【{}】执行失败，耗时：{}ms", request.getToolName(), System.currentTimeMillis() - start, e);
        return ToolCallResponse.error(request.getToolCallId(), request.getToolName(), "工具执行遇到问题，请稍后重试");
    }

    @Override
    public String getName() {
        return "ToolMonitoringInterceptor";
    }
}
