package com.agenthub.infrastructure.tools.skills_tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 技能工具回调提供者，将已安装的技能转换为Spring AI的ToolCallback。
 * 
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkillToolCallbackProvider implements ToolCallbackProvider {
    
    private final SkillFileManager skillFileManager;
    private final SkillExecutor skillExecutor;
    private final ObjectMapper objectMapper;
    
    @Override
    public ToolCallback[] getToolCallbacks() {
        List<SkillPackage> skills = skillFileManager.getValidSkills();
        
        List<ToolCallback> callbacks = skills.stream()
                .filter(this::shouldRegisterAsTool)
                .map(this::createToolCallback)
                .toList();
        
        log.info("Registered {} skill tools", callbacks.size());
        return callbacks.toArray(new ToolCallback[0]);
    }
    
    /**
     * 判断是否应该注册为工具。
     */
    private boolean shouldRegisterAsTool(SkillPackage skillPackage) {
        return skillPackage.isInstalled() 
                && skillPackage.isValid()
                && skillPackage.getManifest() != null;
    }
    
    /**
     * 创建工具回调。
     */
    private ToolCallback createToolCallback(SkillPackage skillPackage) {
        return new SkillToolCallback(
                skillPackage.getManifest(), 
                skillExecutor, 
                objectMapper
        );
    }
    
    /**
     * 根据技能代码获取ToolCallback。
     */
    public ToolCallback getToolCallback(String skillCode) {
        return skillFileManager.getSkill(skillCode)
                .filter(this::shouldRegisterAsTool)
                .map(this::createToolCallback)
                .orElse(null);
    }
    
    /**
     * 根据技能代码列表获取ToolCallback数组。
     */
    public ToolCallback[] getToolCallbacks(List<String> skillCodes) {
        List<ToolCallback> callbacks = skillCodes.stream()
                .map(this::getToolCallback)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        
        return callbacks.toArray(new ToolCallback[0]);
    }
}
