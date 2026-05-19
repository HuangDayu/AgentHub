package com.agenthub.infrastructure.tools.skills_tools;

import com.agenthub.domain.model.tools.Skill;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 技能工具回调提供者，将技能转换为Spring AI的ToolCallback。
 *
 * @author huangdayu
 */
@Deprecated
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillToolCallbackProvider implements ToolCallbackProvider {

    private final SkillFileManager skillFileManager;
    private final ObjectMapper objectMapper;

    @Override
    public ToolCallback[] getToolCallbacks() {
        return getToolCallbacks(skillFileManager.getAllSkills().values());
    }

    public ToolCallback[] getToolCallbacks(Collection<Skill> skills) {
        List<ToolCallback> callbacks = skills.parallelStream()
                .map(this::createToolCallback)
                .toList();
        log.info("Registered {} skill tools", callbacks.size());
        return callbacks.toArray(new ToolCallback[0]);
    }

    private ToolCallback createToolCallback(Skill skill) {
        return new SkillToolCallback(skill, objectMapper);
    }
}
