package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentSkillDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * 技能数据域工具，提供技能信息查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "SkillTools", description = "技能数据工具，提供技能信息查询（已脱敏）")
public class SkillTools {

    private final SkillRepository skillRepository;

    private ReActAgentContext getAgentContext(ToolContext toolContext) {
        return (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
    }

    @Tool(description = "获取当前工作空间下Agent可用的技能列表")
    public List<AgentSkillDTO> getSkills(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return skillRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentSkillDTO.class))
                .collect(Collectors.toList());
    }
}
