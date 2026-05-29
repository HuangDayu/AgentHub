package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 知识数据域工具，提供知识库、检索策略、向量库配置查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "KnowledgeTools", description = "知识数据工具，提供知识库与检索配置查询（已脱敏）")
public class KnowledgeTools {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final VectorStoreConfigRepository vectorStoreConfigRepository;



    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取当前租户下的知识库列表")
    public List<AgentKnowledgeBaseDTO> getKnowledgeBases(ToolContext toolContext) {
        return knowledgeBaseRepository.findByTenantId(getWorkspace(toolContext).getTenantId()).stream()
                .map(k -> BeanUtil.copyProperties(k, AgentKnowledgeBaseDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的检索策略列表")
    public List<AgentRetrievalStrategyDTO> getRetrievalStrategies(ToolContext toolContext) {
        return retrievalStrategyRepository.findByWorkspace(getWorkspace(toolContext).getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentRetrievalStrategyDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前租户下的向量库配置列表（不含主机地址、API密钥等敏感信息）")
    public List<AgentVectorStoreConfigDTO> getVectorStoreConfigs(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return vectorStoreConfigRepository.findAllByTenantId(ctx.getAgent().getTenantId()).stream()
                .map(v -> BeanUtil.copyProperties(v, AgentVectorStoreConfigDTO.class))
                .collect(Collectors.toList());
    }
}
