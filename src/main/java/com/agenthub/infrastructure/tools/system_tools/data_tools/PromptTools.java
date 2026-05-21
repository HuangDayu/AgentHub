package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.domain.model.PromptTemplateInfo;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentPromptTemplateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;

/**
 * 提示模板数据域工具，提供提示模板信息查询与创建（不含模板内容）。
 */
@RequiredArgsConstructor
@AgentTools(name = "PromptTools", description = "提示模板数据工具，提供提示模板信息查询与创建（不含模板内容）")
public class PromptTools {

    private final PromptTemplateRepository promptTemplateRepository;

    private ReActAgentContext getAgentContext(ToolContext toolContext) {
        return (ReActAgentContext) toolContext.getContext().get(AGENT_CONTEXT_KEY);
    }

    @Tool(description = "获取当前工作空间下的提示模板列表（不含模板内容）")
    public List<AgentPromptTemplateDTO> getPromptTemplates(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return promptTemplateRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentPromptTemplateDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "创建新的提示模板")
    public AgentPromptTemplateDTO createPromptTemplate(ToolContext toolContext,
                                                         @ToolParam(description = "模板名称") String name,
                                                         @ToolParam(description = "模板描述") String description,
                                                         @ToolParam(description = "模板类别(SYSTEM/USER/ASSISTANT/GENERAL)") String category,
                                                         @ToolParam(description = "模板内容") String content,
                                                         @ToolParam(description = "是否启用", required = false) Boolean isActive) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        PromptTemplateInfo template = new PromptTemplateInfo();
        template.setTenantId(ctx.getAgent().getTenantId());
        template.setWorkspaceId(ctx.getWorkspace().getWorkspace().getId());
        template.setName(name);
        template.setDescription(description);
        template.setCategory(PromptTemplateInfo.Category.valueOf(category.toUpperCase()));
        template.setContent(content);
        template.setActive(isActive != null && isActive);
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        return BeanUtil.copyProperties(promptTemplateRepository.save(template), AgentPromptTemplateDTO.class);
    }
}
