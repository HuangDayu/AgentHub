package com.agenthub.infrastructure.tools.skills_tools;

import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;
import com.agenthub.domain.model.tools.Skill;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.enums.AgentToolType.SKILL_TOOL;

/**
 * @author huangdayu
 */
@RequiredArgsConstructor
@Component
public class SkillToolsFactory implements AbstractToolsFactory {

    private final SkillToolCallbackProvider skillToolCallbackProvider;
    private final SkillRepository skillRepository;
    private final SkillFileManager skillFileManager;

    @Override
    public AgentToolType getToolInfo() {
        return SKILL_TOOL;
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
    public Set<ToolCallback> getToolCallbacks(List<AgentToolInfo> toolIds) {
        Set<String> collect = toolIds.parallelStream().map(AgentToolInfo::getName).collect(Collectors.toSet());
        Set<Skill> collect1 = skillFileManager.getAllSkills().values().parallelStream().filter(skill -> collect.contains(skill.getName())).collect(Collectors.toSet());
        return Set.of(skillToolCallbackProvider.getToolCallbacks(collect1));
    }
}
