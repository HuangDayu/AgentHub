package com.agenthub.infrastructure.tools.function_tools;

import cn.hutool.core.collection.CollUtil;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import com.agenthub.domain.model.AgentToolInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.agenthub.domain.model.AgentToolType.FUNCTION_TOOLS;

/**
 * @author huangdayu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionToolsFactory implements AbstractToolsFactory {

    private static final Set<ToolCallback> THINGS_TOOL_CALLBACKS = new CopyOnWriteArraySet<>();


    public void addToolCallback(List<ToolCallback> toolCallbacks) {
        if (CollUtil.isNotEmpty(toolCallbacks)) {
            THINGS_TOOL_CALLBACKS.addAll(toolCallbacks);
        }
    }

    public void removeToolCallback(List<ToolCallback> toolCallbacks) {
        if (CollUtil.isNotEmpty(toolCallbacks)) {
            toolCallbacks.forEach(THINGS_TOOL_CALLBACKS::remove);
        }
    }

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(FUNCTION_TOOLS);
    }

    public Set<ToolCallback> getToolCallbacks() {
        return THINGS_TOOL_CALLBACKS;
    }

    public void clearToolCallbacks() {
        THINGS_TOOL_CALLBACKS.clear();
    }

}
