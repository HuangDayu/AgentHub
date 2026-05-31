package com.agenthub.domain.model.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流边值对象。
 * 表示工作流中节点之间的连接关系。
 *
 * @author huangdayu
 */
@Data
public class DagWorkflowEdge {

    /** 边ID */
    private final String id;

    /** 源节点ID */
    private final String sourceNodeId;

    /** 目标节点ID */
    private final String targetNodeId;

    @JsonCreator
    public DagWorkflowEdge(@JsonProperty("id") String id,
                       @JsonProperty("sourceNodeId") String sourceNodeId,
                       @JsonProperty("targetNodeId") String targetNodeId) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
    }

    /** 边标签（用于条件分支） */
    private String label;

    /** 条件表达式 */
    private String condition;

    /** 扩展数据 */
    private Map<String, Object> metadata;

    /**
     * 创建工作流边。
     *
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @return 工作流边实例
     */
    public static DagWorkflowEdge create(String sourceNodeId, String targetNodeId) {
        return new DagWorkflowEdge(randomId(), sourceNodeId, targetNodeId);
    }

    /**
     * 创建带条件的边。
     *
     * @param sourceNodeId 源节点ID
     * @param targetNodeId 目标节点ID
     * @param condition 条件表达式
     * @return 工作流边实例
     */
    public static DagWorkflowEdge createWithCondition(String sourceNodeId,
                                                   String targetNodeId,
                                                   String condition) {
        DagWorkflowEdge edge = new DagWorkflowEdge(randomId(), sourceNodeId, targetNodeId);
        edge.condition = condition;
        return edge;
    }

    /**
     * 判断是否为无条件边。
     *
     * @return 如果无条件返回true
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isUnconditional() {
        return condition == null || condition.isBlank();
    }

    /**
     * 判断是否连接到指定节点。
     *
     * @param nodeId 节点ID
     * @return 如果连接到该节点返回true
     */
    public boolean connectsTo(String nodeId) {
        return targetNodeId.equals(nodeId);
    }
}
