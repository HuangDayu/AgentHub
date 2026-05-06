package com.agenthub.infrastructure.tools.skills_tools;

import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import com.agenthub.domain.model.AgentToolInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.agenthub.domain.model.AgentToolType.SKILL_TOOLS;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class SkillToolsFactory implements AbstractToolsFactory {

    private final SkillToolCallbackProvider skillToolCallbackProvider;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(SKILL_TOOLS);
    }

    @Override
    public Set<ToolCallback> getToolCallbacks() {
        return Set.of(skillToolCallbackProvider.getToolCallbacks());
    }
}
