package com.agenthub.infrastructure.tools.system_tools.core_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.ModelConfig;
import com.agenthub.domain.model.Workspace;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 模型数据域工具，提供模型配置与模型策略查询（已脱敏）。
 */
@RequiredArgsConstructor
@AgentTools(name = "ModelTools", description = "模型数据工具，提供模型配置与策略查询（已脱敏）")
public class ModelTools {

    private final ModelConfigRepository modelConfigRepository;
    private final ModelStrategyRepository modelStrategyRepository;

    private Workspace getWorkspace(ToolContext toolContext) {
        return getAgentContext(toolContext).getWorkspace().getWorkspace();
    }

    @Tool(description = "获取所有已启用的模型配置（不含API密钥等敏感信息）")
    public List<AgentModelConfigDTO> getModelConfigs() {
        return modelConfigRepository.findEnabledAll(true).stream()
                .map(c -> BeanUtil.copyProperties(c, AgentModelConfigDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取当前工作空间下的模型策略列表")
    public List<AgentModelStrategyDTO> getModelStrategies(ToolContext toolContext) {
        return modelStrategyRepository.findByWorkspace(getWorkspace(toolContext).getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentModelStrategyDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "获取模型能力摘要（包含擅长领域、成本等级、速度等级），用于模型选择决策")
    public List<ModelCapabilitySummary> getModelCapabilities() {
        return modelConfigRepository.findEnabledAll(true).stream()
                .map(this::toCapabilitySummary)
                .collect(Collectors.toList());
    }

    @Tool(description = "根据任务特征推荐最适合的模型。返回推荐模型和理由。")
    public ModelRecommendation recommendModel(
            @ToolParam(description = "任务描述，如'代码生成'、'文本分析'、'多模态理解'") String taskDescription,
            @ToolParam(description = "复杂度：LOW=简单查询/格式化, MEDIUM=一般推理/生成, HIGH=复杂推理/创作") String complexity) {
        List<ModelConfig> models = modelConfigRepository.findEnabledAll(true);
        if (models.isEmpty()) return null;
        return findBestModel(models, taskDescription, complexity);
    }

    private ModelCapabilitySummary toCapabilitySummary(ModelConfig config) {
        ModelCapabilitySummary summary = new ModelCapabilitySummary();
        summary.setModelConfigId(config.getId());
        summary.setModelName(config.getModel());
        summary.setSupplier(config.getSupplier() != null ? config.getSupplier().name() : "UNKNOWN");
        summary.setCapabilityDomain(inferCapabilityDomain(config.getModel()));
        summary.setCostLevel(inferCostLevel(config.getSupplier()));
        summary.setSpeedLevel(inferSpeedLevel(config.getModel()));
        summary.setMaxTokens(inferMaxTokens(config.getModel()));
        summary.setAvailable(true);
        return summary;
    }

    private String inferCapabilityDomain(String modelName) {
        if (modelName == null) return "通用";
        String lower = modelName.toLowerCase();
        if (lower.contains("code") || lower.contains("coder") || lower.contains("deepseek")) return "代码生成";
        if (lower.contains("vision") || lower.contains("vl") || lower.contains("multimodal")) return "多模态";
        if (lower.contains("embedding")) return "文本嵌入";
        if (lower.contains("search") || lower.contains("retrieval")) return "检索";
        if (lower.contains("flash") || lower.contains("mini") || lower.contains("lite")) return "快速响应";
        return "通用";
    }

    private String inferCostLevel(com.agenthub.domain.enums.ModelSupplier supplier) {
        if (supplier == null) return "MEDIUM";
        String name = supplier.name().toLowerCase();
        if (name.contains("ollama") || name.contains("local")) return "LOW";
        if (name.contains("openai") && name.contains("gpt-4")) return "HIGH";
        if (name.contains("anthropic")) return "HIGH";
        return "MEDIUM";
    }

    private String inferSpeedLevel(String modelName) {
        if (modelName == null) return "MEDIUM";
        String lower = modelName.toLowerCase();
        if (lower.contains("mini") || lower.contains("flash") || lower.contains("lite") || lower.contains("fast")) return "FAST";
        if (lower.contains("plus") || lower.contains("pro") || lower.contains("max") || lower.contains("ultra")) return "SLOW";
        return "MEDIUM";
    }

    private int inferMaxTokens(String modelName) {
        if (modelName == null) return 4096;
        String lower = modelName.toLowerCase();
        if (lower.contains("gpt-4o")) return 128000;
        if (lower.contains("gpt-4")) return 8192;
        if (lower.contains("claude-3")) return 200000;
        if (lower.contains("qwen-max")) return 32000;
        if (lower.contains("qwen-plus")) return 131072;
        if (lower.contains("deepseek")) return 65536;
        return 4096;
    }

    private ModelRecommendation findBestModel(List<ModelConfig> models,
                                               String taskDescription, String complexity) {
        String taskDomain = inferCapabilityDomain(taskDescription);
        int complexityScore = parseComplexity(complexity);

        ModelConfig best = models.stream()
                .sorted(Comparator.comparingInt(m -> calculateMatchScore(m, taskDomain, complexityScore)))
                .findFirst()
                .orElse(models.getFirst());

        ModelRecommendation rec = new ModelRecommendation();
        rec.setModelConfigId(best.getId());
        rec.setModelName(best.getModel());
        rec.setSupplier(best.getSupplier() != null ? best.getSupplier().name() : "UNKNOWN");
        rec.setReason(buildRecommendationReason(best, taskDomain, complexityScore));
        rec.setConfidence(calculateConfidence(models.size()));
        return rec;
    }

    private int calculateMatchScore(ModelConfig model, String taskDomain, int complexity) {
        int score = 0;
        String modelDomain = inferCapabilityDomain(model.getModel());

        if (taskDomain.equals(modelDomain) || "通用".equals(modelDomain)) score += 10;
        if ("代码生成".equals(taskDomain) && modelDomain.equals("代码生成")) score += 20;
        if ("多模态".equals(taskDomain) && modelDomain.equals("多模态")) score += 20;

        String speed = inferSpeedLevel(model.getModel());
        if (complexity <= 1 && "FAST".equals(speed)) score += 5;
        if (complexity >= 3 && "SLOW".equals(speed)) score += 5;

        return score;
    }

    private int parseComplexity(String complexity) {
        if (complexity == null) return 2;
        return switch (complexity.toUpperCase()) {
            case "LOW" -> 1;
            case "HIGH" -> 3;
            default -> 2;
        };
    }

    private String buildRecommendationReason(ModelConfig model, String taskDomain, int complexity) {
        String modelDomain = inferCapabilityDomain(model.getModel());
        String speed = inferSpeedLevel(model.getModel());

        StringBuilder reason = new StringBuilder();
        if (taskDomain.equals(modelDomain)) {
            reason.append("模型擅长").append(taskDomain).append("；");
        }
        if (complexity <= 1 && "FAST".equals(speed)) {
            reason.append("快速响应适合简单任务；");
        }
        if (complexity >= 3) {
            reason.append("适合复杂推理任务；");
        }
        if (reason.isEmpty()) {
            reason.append("综合能力匹配");
        }
        return reason.toString();
    }

    private String calculateConfidence(int modelCount) {
        if (modelCount <= 1) return "HIGH";
        if (modelCount <= 3) return "MEDIUM";
        return "LOW";
    }
}
