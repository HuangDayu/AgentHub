package com.agenthub.infrastructure.tools.skills_tools;

import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.domain.model.Skill;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.model.AgentToolType.SKILL_TOOLS;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class SkillToolsFactory implements AbstractToolsFactory {

    private final SkillToolCallbackProvider skillToolCallbackProvider;
    private final SkillRepository skillRepository;

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(SKILL_TOOLS);
    }

    @Override
    public Set<ToolCallback> getAllToolCallbacks() {
        return Set.of(skillToolCallbackProvider.getToolCallbacks());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(String name) {
        return getAllToolCallbacks().stream()
                .filter(toolCallback -> toolCallback.getToolDefinition().name().equals(name))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(List<String> toolIds) {
        List<Skill> skills = skillRepository.findByIds(toolIds);
        return Set.of(skillToolCallbackProvider.getToolCallbacks(skills));
    }
}
