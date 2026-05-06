package com.agenthub.application.port.out.agent;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

/**
 * Agent端口接口，定义Agent的核心操作。
 * 
 * @author huangdayu
 */
public interface AgentPort {
    
    /**
     * 获取Agent名称。
     */
    String getName(String agentId);
    
    /**
     * 流式对话。
     */
    Flux<Message> streamMessages(String agentId, String userMessage);
    
    /**
     * 同步对话。
     */
    AssistantMessage call(String agentId, String userMessage);
    
    /**
     * 中断Agent执行。
     */
    void interrupt(String agentId);
}
