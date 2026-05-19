package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.workflow.ExecutionCommand;
import com.agenthub.application.dto.workflow.ExecutionOutput;
import com.agenthub.application.port.out.workflow.WorkflowExecutionPort;
import com.agenthub.application.port.out.workflow.WorkflowStatePort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.workflow.NodeResult;
import com.agenthub.domain.model.workflow.WorkflowContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 工作流执行用例。
 * 负责工作流的执行、停止和结果获取。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class WorkflowExecutionUseCase {

    private final WorkflowExecutionPort executionPort;
    private final WorkflowStatePort statePort;

    /**
     * 初始化工作流执行。
     *
     * @param command 执行命令
     * @return 执行输出
     */
    public Mono<ExecutionOutput> initialize(ExecutionCommand command) {
        return executionPort.initializeContext(command)
                .flatMap(statePort::saveContext)
                .map(this::toOutput);
    }

    /**
     * 执行工作流并返回节点执行事件流。
     *
     * @param command 执行命令
     * @return 节点执行结果流
     */
    public Flux<NodeResult> execute(ExecutionCommand command) {
        return executionPort.initializeContext(command)
                .flatMapMany(executionPort::executeWorkflow);
    }

    /**
     * 停止工作流执行。
     *
     * @param executionId 执行ID
     * @return 完成信号
     */
    public Mono<Void> stop(String executionId) {
        return executionPort.stopExecution(executionId)
                .then(statePort.deleteContext(executionId));
    }

    /**
     * 获取执行结果。
     *
     * @param executionId 执行ID
     * @return 执行输出
     */
    public Mono<ExecutionOutput> getResult(String executionId) {
        return statePort.loadContext(executionId)
                .map(opt -> opt.orElseThrow(() -> 
                        new NotFoundException("Execution not found: " + executionId)))
                .map(this::toOutput);
    }

    /**
     * 转换为输出对象。
     *
     * @param context 执行上下文
     * @return 执行输出
     */
    private ExecutionOutput toOutput(WorkflowContext context) {
        return BeanUtil.copyProperties(context, ExecutionOutput.class);
    }
}
