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
import java.util.Map;
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

    @Tool(description = "获取当前租户下的向量库配置列表（不含敏感信息）")
    public List<AgentVectorStoreConfigDTO> getVectorStoreConfigs(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return vectorStoreConfigRepository.findAllByTenantId(ctx.getAgent().getTenantId()).stream()
                .map(v -> BeanUtil.copyProperties(v, AgentVectorStoreConfigDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取知识库摘要（包含文档数量、描述、更新时间），用于知识库选择决策")
    public List<KnowledgeBaseSummary> getKnowledgeBaseSummaries(ToolContext toolContext) {
        List<KnowledgeBase> kbs = knowledgeBaseRepository.findByTenantId(
                getWorkspace(toolContext).getTenantId());
        Map<String, Integer> docCounts = batchCountDocuments(kbs);
        return kbs.stream()
                .map(kb -> toSummary(kb, docCounts.getOrDefault(kb.getId(), 0)))
                .collect(Collectors.toList());
    }

    @Tool(description = "根据查询主题推荐最相关的知识库")
    public List<KnowledgeBaseRecommendation> recommendKnowledgeBase(
            @ToolParam(description = "查询主题或关键词") String topic,
            ToolContext toolContext) {
        List<KnowledgeBase> kbs = knowledgeBaseRepository.findByTenantId(
                getWorkspace(toolContext).getTenantId());
        Map<String, Integer> docCounts = batchCountDocuments(kbs);
        return kbs.stream()
                .map(kb -> toRecommendation(kb, topic, docCounts.getOrDefault(kb.getId(), 0)))
                .sorted((a, b) -> compareRelevance(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取知识库的文档数量")
    public int getKnowledgeBaseDocumentCount(
            @ToolParam(description = "知识库ID") String knowledgeBaseId) {
        return countDocuments(knowledgeBaseId);
    }

    private Map<String, Integer> batchCountDocuments(List<KnowledgeBase> kbs) {
        return kbs.stream()
                .collect(Collectors.toMap(
                        KnowledgeBase::getId,
                        kb -> countDocuments(kb.getId()),
                        (a, b) -> a
                ));
    }

    private int countDocuments(String kbId) {
        try { return ingestionDocumentRepository.findByKbId(kbId).size(); }
        catch (Exception e) { return 0; }
    }

    private KnowledgeBaseSummary toSummary(KnowledgeBase kb, int docCount) {
        KnowledgeBaseSummary summary = new KnowledgeBaseSummary();
        summary.setKnowledgeBaseId(kb.getId());
        summary.setName(kb.getName());
        summary.setDescription(kb.getDescription());
        summary.setDocumentCount(docCount);
        summary.setLastUpdatedAt(kb.getUpdatedAt() != null ? kb.getUpdatedAt().toString() : "未知");
        summary.setSupportedQueryTypes(List.of("语义检索", "关键词检索"));
        return summary;
    }

    private KnowledgeBaseRecommendation toRecommendation(KnowledgeBase kb, String topic, int docCount) {
        KnowledgeBaseRecommendation rec = new KnowledgeBaseRecommendation();
        rec.setKnowledgeBaseId(kb.getId());
        rec.setName(kb.getName());
        rec.setReason(buildRecommendationReason(kb, topic, docCount));
        rec.setRelevanceScore(calculateRelevance(kb, topic, docCount));
        return rec;
    }

    private String buildRecommendationReason(KnowledgeBase kb, String topic, int docCount) {
        if (topic == null || topic.isBlank()) return "所有可用知识库";
        String lower = topic.toLowerCase();
        if (kb.getName() != null && kb.getName().toLowerCase().contains(lower)) {
            return "知识库名称与查询主题匹配";
        }
        if (kb.getDescription() != null && kb.getDescription().toLowerCase().contains(lower)) {
            return "知识库描述与查询主题相关";
        }
        if (docCount > 0) return "包含" + docCount + "篇文档，可能包含相关信息";
        return "知识库可用但文档数量未知";
    }

    private String calculateRelevance(KnowledgeBase kb, String topic, int docCount) {
        if (topic == null || topic.isBlank()) return "MEDIUM";
        double score = keywordMatchScore(kb, topic) * 0.5 + docCountScore(docCount) * 0.3 + freshnessScore(kb) * 0.2;
        if (score >= 0.7) return "HIGH";
        if (score >= 0.4) return "MEDIUM";
        return "LOW";
    }

    private double keywordMatchScore(KnowledgeBase kb, String topic) {
        String lower = topic.toLowerCase();
        if (kb.getName() != null && kb.getName().toLowerCase().contains(lower)) return 1.0;
        if (kb.getDescription() != null && kb.getDescription().toLowerCase().contains(lower)) return 0.6;
        return 0.0;
    }

    private double docCountScore(int count) {
        if (count > 100) return 1.0;
        if (count > 10) return 0.6;
        if (count > 0) return 0.3;
        return 0.0;
    }

    private double freshnessScore(KnowledgeBase kb) {
        if (kb.getUpdatedAt() == null) return 0.0;
        long days = java.time.Duration.between(kb.getUpdatedAt(), java.time.Instant.now()).toDays();
        if (days < 7) return 1.0;
        if (days < 30) return 0.6;
        return 0.2;
    }

    private int compareRelevance(String a, String b) {
        return getScore(a) - getScore(b);
    }

    private int getScore(String score) {
        if (score == null) return 0;
        return switch (score.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
