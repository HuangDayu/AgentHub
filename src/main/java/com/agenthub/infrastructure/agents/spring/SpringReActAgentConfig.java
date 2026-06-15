package com.agenthub.infrastructure.agents.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Spring AI Agent 运行时配置。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpringReActAgentConfig {

    private ChatModel chatModel;

    private String systemPrompt;

    private List<ToolCallback> tools;

    private List<Advisor> advisors;
}
