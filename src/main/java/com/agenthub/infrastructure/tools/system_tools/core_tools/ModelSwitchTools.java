package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.usecase.AgentPoolUseCase;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import com.agenthub.domain.model.agent.AgentConfig;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.core_tools.dto.ModelSwitchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Optional;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 模型切换工具，支持运行时动态切换模型。
 */
@RequiredArgsConstructor
@AgentTools(name = "ModelSwitchTools", description = "模型切换工具，支持运行时动态切换当前会话的模型")
public class ModelSwitchTools {

    private final AgentConfigRepository agentConfigRepository;
    private final AgentPoolUseCase agentPoolUseCase;

    @Tool(description = "切换当前会话的模型，下一轮对话将使用新模型")
    public ModelSwitchResult switchModel(
            @ToolParam(description = "目标模型配置ID") String modelConfigId,
            ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        String agentId = ctx.getAgent().getId();
        String sessionId = ctx.getSessionId();
        boolean updated = updateModelConfig(agentId, modelConfigId);
        if (!updated) return buildResult(modelConfigId, "未找到可更新的模型配置");
        agentPoolUseCase.evict(agentId, sessionId);
        return buildResult(modelConfigId, "模型已切换，下一轮生效");
    }

    @Tool(description = "获取当前会话使用的模型信息")
    public ModelSwitchResult getCurrentModel(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        String modelId = ctx.getChatModelId();
        if (modelId == null) return buildResult("未配置", "当前未设置模型");
        AgentConfig config = agentConfigRepository.findById(modelId).orElse(null);
        if (config == null) return buildResult(modelId, "模型配置不存在");
        return buildResult(config.getId(), config.getName());
    }

    private boolean updateModelConfig(String agentId, String newModelConfigId) {
        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        Optional<AgentConfig> modelConfig = configs.stream()
                .filter(c -> c.getCategory() == AgentConfigCategory.MODEL
                        && c.getType() == AgentConfigType.CHAT_MODEL)
                .findFirst();
        modelConfig.ifPresent(c -> {
            c.setConfigId(newModelConfigId);
            agentConfigRepository.saveOrUpdate(c);
        });
        return modelConfig.isPresent();
    }

    private ModelSwitchResult buildResult(String modelId, String message) {
        ModelSwitchResult result = new ModelSwitchResult();
        result.setModelConfigId(modelId);
        result.setMessage(message);
        return result;
    }
}
