package com.agenthub.domain.model.workflow;

import com.agenthub.domain.enums.workflow.DagNodeStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * 节点执行结果值对象。
 * 记录节点执行后的输出数据和状态信息。
 *
 * @author huangdayu
 */
@Data
public class NodeResult {

    /** 节点ID */
    private final String nodeId;

    /** 执行状态 */
    private final DagNodeStatus status;

    /** 输出数据 */
    private final Map<String, Object> outputs;

    @JsonCreator
    public NodeResult(@JsonProperty("nodeId") String nodeId,
                      @JsonProperty("status") DagNodeStatus status,
                      @JsonProperty("outputs") Map<String, Object> outputs) {
        this.nodeId = nodeId;
        this.status = status;
        this.outputs = outputs;
    }

    /** 错误信息 */
    private String errorMessage;

    /** 开始时间 */
    private Instant startTime;

    /** 结束时间 */
    private Instant endTime;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /**
     * 创建成功的结果。
     *
     * @param nodeId 节点ID
     * @param outputs 输出数据
     * @return 成功结果
     */
    public static NodeResult success(String nodeId, Map<String, Object> outputs) {
        return new NodeResult(nodeId, DagNodeStatus.SUCCESS, outputs);
    }

    /**
     * 创建失败的结果。
     *
     * @param nodeId 节点ID
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static NodeResult failure(String nodeId, String errorMessage) {
        NodeResult result = new NodeResult(nodeId, DagNodeStatus.FAILED, Map.of());
        result.errorMessage = errorMessage;
        return result;
    }

    /**
     * 判断是否执行成功。
     *
     * @return 如果成功返回true
     */
    public boolean isSuccess() {
        return status == DagNodeStatus.SUCCESS;
    }

    /**
     * 获取指定输出值。
     *
     * @param key 输出键
     * @return 输出值
     */
    public Object getOutput(String key) {
        return outputs != null ? outputs.get(key) : null;
    }

    /**
     * 计算执行耗时。
     *
     * @return 执行耗时（毫秒）
     */
    public long calculateDuration() {
        if (startTime != null && endTime != null) {
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
        return durationMs;
    }
}
