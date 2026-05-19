package com.agenthub.domain.model.workflow;

import com.agenthub.domain.enums.workflow.NodeStatus;
import com.agenthub.domain.enums.workflow.NodeType;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 工作流节点实体。
 * 表示工作流中的一个执行节点。
 *
 * @author huangdayu
 */
@Data
public class WorkflowNode {

    /** 节点ID */
    private final String id;

    /** 节点类型 */
    private final NodeType type;

    /** 节点名称 */
    private String name;

    /** 节点描述 */
    private String description;

    /** 节点配置 */
    private NodeConfig config;

    /** 节点位置 */
    private NodePosition position;

    /** 节点状态 */
    private NodeStatus status;

    /** 扩展数据 */
    private Map<String, Object> metadata;

    /** 创建时间 */
    private Instant createdAt;

    /** 更新时间 */
    private Instant updatedAt;

    /**
     * 创建工作流节点。
     *
     * @param type 节点类型
     * @param name 节点名称
     * @return 工作流节点实例
     */
    public static WorkflowNode create(NodeType type, String name) {
        WorkflowNode node = new WorkflowNode(randomId(), type);
        node.name = name;
        node.status = NodeStatus.PENDING;
        node.config = NodeConfig.defaultConfig();
        node.position = NodePosition.defaultPosition();
        node.createdAt = Instant.now();
        node.updatedAt = Instant.now();
        return node;
    }

    /**
     * 更新节点状态。
     *
     * @param newStatus 新状态
     */
    public void updateStatus(NodeStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新节点配置。
     *
     * @param newConfig 新配置
     */
    public void updateConfig(NodeConfig newConfig) {
        this.config = newConfig;
        this.updatedAt = Instant.now();
    }

    /**
     * 判断节点是否可执行。
     *
     * @return 如果可执行返回true
     */
    public boolean canExecute() {
        return type.isExecutable() && status.canExecute();
    }
}
