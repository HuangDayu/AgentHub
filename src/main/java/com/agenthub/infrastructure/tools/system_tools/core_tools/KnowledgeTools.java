package com.agenthub.infrastructure.tools.system_tools.core_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

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
    private final IngestionDocumentRepository ingestionDocumentRepository;

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

    @Tool(description = "获取知识库摘要（包含文档数量、描述、更新时间），用于知识库选择决策")
    public List<KnowledgeBaseSummary> getKnowledgeBaseSummaries(ToolContext toolContext) {
        return knowledgeBaseRepository.findByTenantId(getWorkspace(toolContext).getTenantId()).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Tool(description = "根据查询主题推荐最相关的知识库")
    public List<KnowledgeBaseRecommendation> recommendKnowledgeBase(
            @ToolParam(description = "查询主题或关键词") String topic,
            ToolContext toolContext) {
        List<KnowledgeBase> kbs = knowledgeBaseRepository.findByTenantId(
                getWorkspace(toolContext).getTenantId());
        return kbs.stream()
                .map(kb -> toRecommendation(kb, topic))
                .sorted((a, b) -> compareRelevance(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取知识库的文档数量")
    public int getKnowledgeBaseDocumentCount(
            @ToolParam(description = "知识库ID") String knowledgeBaseId) {
        return ingestionDocumentRepository.findByKbId(knowledgeBaseId).size();
    }

    private KnowledgeBaseSummary toSummary(KnowledgeBase kb) {
        KnowledgeBaseSummary summary = new KnowledgeBaseSummary();
        summary.setKnowledgeBaseId(kb.getId());
        summary.setName(kb.getName());
        summary.setDescription(kb.getDescription());
        summary.setDocumentCount(countDocuments(kb.getId()));
        summary.setLastUpdatedAt(kb.getUpdatedAt() != null ? kb.getUpdatedAt().toString() : "未知");
        summary.setSupportedQueryTypes(List.of("语义检索", "关键词检索"));
        return summary;
    }

    private int countDocuments(String kbId) {
        try {
            return ingestionDocumentRepository.findByKbId(kbId).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private KnowledgeBaseRecommendation toRecommendation(KnowledgeBase kb, String topic) {
        KnowledgeBaseRecommendation rec = new KnowledgeBaseRecommendation();
        rec.setKnowledgeBaseId(kb.getId());
        rec.setName(kb.getName());
        rec.setReason(buildRecommendationReason(kb, topic));
        rec.setRelevanceScore(calculateRelevance(kb, topic));
        return rec;
    }

    private String buildRecommendationReason(KnowledgeBase kb, String topic) {
        if (topic == null || topic.isBlank()) return "所有可用知识库";
        String lowerTopic = topic.toLowerCase();
        if (kb.getName() != null && kb.getName().toLowerCase().contains(lowerTopic)) {
            return "知识库名称与查询主题匹配";
        }
        if (kb.getDescription() != null && kb.getDescription().toLowerCase().contains(lowerTopic)) {
            return "知识库描述与查询主题相关";
        }
        int docCount = countDocuments(kb.getId());
        if (docCount > 0) {
            return "包含" + docCount + "篇文档，可能包含相关信息";
        }
        return "知识库可用但文档数量未知";
    }

    private String calculateRelevance(KnowledgeBase kb, String topic) {
        if (topic == null || topic.isBlank()) return "MEDIUM";
        String lowerTopic = topic.toLowerCase();
        if (kb.getName() != null && kb.getName().toLowerCase().contains(lowerTopic)) {
            return "HIGH";
        }
        if (kb.getDescription() != null && kb.getDescription().toLowerCase().contains(lowerTopic)) {
            return "MEDIUM";
        }
        int docCount = countDocuments(kb.getId());
        if (docCount > 10) return "MEDIUM";
        if (docCount > 0) return "LOW";
        return "LOW";
    }

    private int compareRelevance(String a, String b) {
        return getRelevanceScore(a) - getRelevanceScore(b);
    }

    private int getRelevanceScore(String score) {
        if (score == null) return 0;
        return switch (score.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
