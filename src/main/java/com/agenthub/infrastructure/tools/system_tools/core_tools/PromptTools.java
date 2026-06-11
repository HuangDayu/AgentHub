package com.agenthub.infrastructure.tools.system_tools.core_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.PromptTemplateRepository;
import com.agenthub.domain.model.PromptTemplateInfo;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.AgentPromptTemplateDTO;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.TemplateSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 提示模板数据域工具，提供提示模板信息查询与创建（不含模板内容）。
 */
@RequiredArgsConstructor
@AgentTools(name = "PromptTools", description = "提示模板数据工具，提供提示模板信息查询与创建（不含模板内容）")
public class PromptTools {

    private final PromptTemplateRepository promptTemplateRepository;



    @Tool(description = "获取当前工作空间下的提示模板列表（不含模板内容）")
    public List<AgentPromptTemplateDTO> getPromptTemplates(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return promptTemplateRepository.findByWorkspaceId(ctx.getWorkspace().getWorkspace().getId()).stream()
                .map(t -> BeanUtil.copyProperties(t, AgentPromptTemplateDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 构建PromptTemplateInfo并设置基本字段。
     */
    private PromptTemplateInfo buildTemplate(TemplateSpec spec) {
        PromptTemplateInfo template = new PromptTemplateInfo();
        initTemplateContext(template, spec.getCtx());
        template.setName(spec.getName());
        template.setDescription(spec.getDescription());
        template.setCategory(PromptTemplateInfo.Category.valueOf(spec.getCategory().toUpperCase()));
        template.setContent(spec.getContent());
        template.setActive(spec.getActive() != null && spec.getActive());
        return template;
    }

    /** 初始化模板上下文信息。 */
    private void initTemplateContext(PromptTemplateInfo template, ReActAgentContext ctx) {
        template.setTenantId(ctx.getAgent().getTenantId());
        template.setWorkspaceId(ctx.getWorkspace().getWorkspace().getId());
    }

    /**
     * 执行创建模板并持久化。
     */
    private AgentPromptTemplateDTO doCreateTemplate(TemplateSpec spec) {
        PromptTemplateInfo template = buildTemplate(spec);
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        return BeanUtil.copyProperties(promptTemplateRepository.save(template), AgentPromptTemplateDTO.class);
    }

    @Tool(description = "创建新的提示模板")
    public AgentPromptTemplateDTO createPromptTemplate(ToolContext toolContext,
                                                           @ToolParam(description = "模板名称") String name,
                                                           @ToolParam(description = "模板描述") String description,
                                                           @ToolParam(description = "模板类别(SYSTEM/USER/ASSISTANT/GENERAL)") String category,
                                                           @ToolParam(description = "模板内容") String content,
                                                           @ToolParam(description = "是否启用", required = false) Boolean active) {
        TemplateSpec spec = new TemplateSpec(name, description, category, content, active, getAgentContext(toolContext));
        return doCreateTemplate(spec);
    }
}
