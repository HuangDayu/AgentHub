package com.agenthub.application.usecase;

import com.agenthub.application.command.SubagentCommand;
import com.agenthub.application.dto.SubagentOutput;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.Subagent;
import com.agenthub.domain.model.agent.Subsession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 子智能体用例，处理子Agent的创建、查询、修改和停用。
 * 创建子Agent时，若指定了sessionId，则自动创建对应的Subsession。
 */
@Component
@RequiredArgsConstructor
public class SubagentUseCase {

    private final SubagentRepository subagentRepository;
    private final AgentRepository agentRepository;
    private final SubsessionRepository subsessionRepository;

    /**
     * 创建子Agent。若命令中指定了sessionId，则同时创建对应的Subsession。
     *
     * @param command 创建命令
     * @return 子Agent输出DTO
     */
    public SubagentOutput create(SubagentCommand command) {
        agentRepository.findById(command.getParentAgentId())
                .orElseThrow(() -> new NotFoundException("Agent not found: " + command.getParentAgentId()));
        Subagent subagent = Subagent.create(
                command.getTenantId(), command.getWorkspaceId(),
                command.getParentAgentId(), command.getName(),
                command.getDescription(), command.getSystemPrompt(),
                command.getModelConfigId());
        Subagent saved = subagentRepository.save(subagent);
        createSubsessionIfNeeded(command, saved);
        return toOutput(saved);
    }

    /**
     * 若命令中指定了sessionId，则在对应的会话中创建Subsession。
     */
    private void createSubsessionIfNeeded(SubagentCommand command, Subagent subagent) {
        if (command.getSessionId() == null || command.getSessionId().isEmpty()) {
            return;
        }
        Subsession subsession = Subsession.create(
                command.getSessionId(), subagent.getId(), subagent.getName());
        subsessionRepository.save(subsession);
    }

    /**
     * 根据ID获取子Agent。
     *
     * @param id 子Agent ID
     * @return 子Agent输出DTO
     */
    public SubagentOutput get(String id) {
        Subagent subagent = subagentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subagent not found: " + id));
        return toOutput(subagent);
    }

    /**
     * 根据父Agent ID列出所有子Agent。
     *
     * @param parentAgentId 父Agent ID
     * @return 子Agent输出DTO列表
     */
    public List<SubagentOutput> listByParent(String parentAgentId) {
        return subagentRepository.findByParentAgentId(parentAgentId).stream()
                .map(this::toOutput)
                .toList();
    }

    /**
     * 更新子Agent。
     *
     * @param id      子Agent ID
     * @param command 更新命令
     * @return 更新后的子Agent输出DTO
     */
    public SubagentOutput update(String id, SubagentCommand command) {
        Subagent subagent = subagentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subagent not found: " + id));
        subagent.update(command.getName(), command.getDescription(),
                command.getSystemPrompt(), command.getModelConfigId());
        Subagent saved = subagentRepository.save(subagent);
        return toOutput(saved);
    }

    /**
     * 停用子Agent。
     *
     * @param id 子Agent ID
     */
    public void deactivate(String id) {
        Subagent subagent = subagentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subagent not found: " + id));
        subagent.deactivate();
        subagentRepository.save(subagent);
    }

    /**
     * 将领域模型转换为输出DTO。
     */
    private SubagentOutput toOutput(Subagent subagent) {
        return new SubagentOutput(subagent.getId(), subagent.getTenantId(),
                subagent.getWorkspaceId(), subagent.getParentAgentId(),subagent.getName(),
                subagent.getDescription(), subagent.getSystemPrompt(),
                subagent.getModelConfigId(), subagent.getStatus(),
                subagent.getCreatedAt(), subagent.getUpdatedAt());
    }
}
