package com.agenthub.domain.model.workflow;

import com.agenthub.domain.enums.workflow.DagWorkflowStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流执行上下文实体。
 * 维护工作流执行过程中的状态和变量。
 *
 * @author huangdayu
 */
@Data
public class DagWorkflowContext {

    /** 执行ID */
    private final String executionId;

    /** 工作流ID */
    private final String workflowId;

    @JsonCreator
    public DagWorkflowContext(@JsonProperty("executionId") String executionId,
                          @JsonProperty("workflowId") String workflowId) {
        this.executionId = executionId;
        this.workflowId = workflowId;
    }

    /** 工作空间ID */
    private String workspaceId;

    /** 租户ID */
    private String tenantId;

    /** 工作流状态 */
    private DagWorkflowStatus status;

    /** 全局变量 */
    private Map<String, Object> variables;

    /** 节点执行结果 */
    private Map<String, NodeResult> nodeResults;

    /** 当前执行节点ID */
    private String currentNodeId;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

    /** 触发者 */
    private String triggeredBy;

    /** 工作流图 */
    private DagWorkflowGraph graph;

    /**
     * 创建新的执行上下文。
     *
     * @param workflowId 工作流ID
     * @return 执行上下文实例
     */
    public static DagWorkflowContext create(String workflowId) {
        DagWorkflowContext ctx = new DagWorkflowContext(randomId(), workflowId);
        ctx.status = DagWorkflowStatus.EXECUTING;
        ctx.variables = new HashMap<>();
        ctx.nodeResults = new HashMap<>();
        ctx.startTime = Instant.now();
        return ctx;
    }

    /**
     * 设置变量。
     *
     * @param key 变量名
     * @param value 变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 获取变量。
     *
     * @param key 变量名
     * @return 变量值
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 记录节点执行结果。
     *
     * @param result 节点结果
     */
    public void recordNodeResult(NodeResult result) {
        nodeResults.put(result.getNodeId(), result);
    }

    /**
     * 更新执行状态。
     *
     * @param newStatus 新状态
     */
    public void updateStatus(DagWorkflowStatus newStatus) {
        this.status = newStatus;
        if (newStatus.isTerminal()) {
            this.endTime = Instant.now();
        }
    }

    /**
     * 判断工作流是否正在执行。
     *
     * @return 如果正在执行返回true
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isRunning() {
        return status != null && status.isRunning();
    }
}
