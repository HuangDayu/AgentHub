package com.agenthub.application.port.out;

import com.agenthub.domain.model.AgentTeam;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent团队运行时端口接口。
 * <p>
 * 定义Agent团队对话和生命周期管理的抽象接口，
 * 支持多Agent协作模式。
 * </p>
 */
public interface AgentTeamRuntime {
    
    /**
     * 初始化Agent团队运行时实例。
     *
     * @param team AgentTeam领域对象
     * @param config 运行时配置参数
     * @return 运行时实例ID
     */
    String initialize(AgentTeam team, Map<String, Object> config);
    
    /**
     * 启动团队会话。
     *
     * @param instanceId 运行时实例ID
     * @param sessionId 会话ID
     * @param context 会话上下文
     */
    void startSession(String instanceId, String sessionId, Map<String, Object> context);
    
    /**
     * 发送消息并获取团队协作响应。
     *
     * @param instanceId 运行时实例ID
     * @param sessionId 会话ID
     * @param message 用户消息
     * @return 团队协作响应消息
     */
    CompletableFuture<String> sendMessage(String instanceId, String sessionId, String message);
    
    /**
     * 流式发送消息。
     *
     * @param instanceId 运行时实例ID
     * @param sessionId 会话ID
     * @param message 用户消息
     * @param callback 流式响应回调
     */
    void streamMessage(String instanceId, String sessionId, String message, AgentRuntime.StreamCallback callback);
    
    /**
     * 结束会话。
     *
     * @param instanceId 运行时实例ID
     * @param sessionId 会话ID
     */
    void endSession(String instanceId, String sessionId);
    
    /**
     * 销毁团队运行时实例。
     *
     * @param instanceId 运行时实例ID
     */
    void destroy(String instanceId);
    
    /**
     * 获取运行时状态。
     *
     * @param instanceId 运行时实例ID
     * @return 运行时状态
     */
    RuntimeStatus getStatus(String instanceId);
    
    /**
     * 获取运行时框架类型。
     *
     * @return 框架类型标识
     */
    String getFrameworkType();
}
