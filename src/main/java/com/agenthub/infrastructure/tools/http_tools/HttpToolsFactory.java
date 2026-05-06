package com.agenthub.infrastructure.tools.http_tools;

import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import com.agenthub.domain.model.AgentToolInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.agenthub.domain.model.AgentToolType.HTTP_TOOLS;

/**
 * HTTP工具工厂，负责提供HTTP工具的ToolCallback。
 * 
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class HttpToolsFactory implements AbstractToolsFactory {

    private final HttpToolCallbackProvider httpToolCallbackProvider;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(HTTP_TOOLS);
    }

    @Override
    public Set<ToolCallback> getToolCallbacks() {
        return httpToolCallbackProvider.getToolCallbacks();
    }
}
