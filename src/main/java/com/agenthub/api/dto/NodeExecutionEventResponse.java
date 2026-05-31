package com.agenthub.api.dto;

import com.agenthub.domain.enums.workflow.DagNodeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 节点执行事件响应DTO。
 * 用于SSE推送节点执行状态变化事件。
 *
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeExecutionEventResponse {

    /** 事件类型 */
    private String eventType;

    /** 节点ID */
    private String nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 执行状态 */
    private DagNodeStatus status;

    /** 输出数据 */
    private Map<String, Object> outputs;

    /** 错误信息 */
    private String errorMessage;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /**
     * 创建节点开始事件。
     *
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @return 事件响应
     */
    public static NodeExecutionEventResponse start(String nodeId, String nodeName) {
        return new NodeExecutionEventResponse("node_start", nodeId, nodeName, 
                DagNodeStatus.EXECUTING, null, null, Instant.now(), null, 0);
    }

    /**
     * 创建节点完成事件。
     *
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @param outputs 输出数据
     * @return 事件响应
     */
    public static NodeExecutionEventResponse complete(String nodeId, String nodeName, 
                                                      Map<String, Object> outputs) {
        Instant end = Instant.now();
        return new NodeExecutionEventResponse("node_complete", nodeId, nodeName, 
                DagNodeStatus.SUCCESS, outputs, null, null, end, 0);
    }

    /**
     * 创建节点失败事件。
     *
     * @param nodeId 节点ID
     * @param nodeName 节点名称
     * @param errorMessage 错误信息
     * @return 事件响应
     */
    public static NodeExecutionEventResponse fail(String nodeId, String nodeName, 
                                                   String errorMessage) {
        return new NodeExecutionEventResponse("node_error", nodeId, nodeName, 
                DagNodeStatus.FAILED, null, errorMessage, null, Instant.now(), 0);
    }
}
