package com.agenthub.domain.model.workflow;

import lombok.Data;

/**
 * 节点位置值对象。
 * 用于记录节点在可视化编辑器中的位置信息。
 *
 * @author huangdayu
 */
@Data
public class NodePosition {

    /** X坐标 */
    private final double x;

    /** Y坐标 */
    private final double y;

    /**
     * 创建节点位置。
     *
     * @param x X坐标
     * @param y Y坐标
     */
    public NodePosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 创建默认位置（0,0）。
     *
     * @return 默认位置
     */
    public static NodePosition defaultPosition() {
        return new NodePosition(0, 0);
    }

    /**
     * 计算与另一个位置的距离。
     *
     * @param other 另一个位置
     * @return 距离值
     */
    public double distanceTo(NodePosition other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 偏移位置。
     *
     * @param offsetX X偏移量
     * @param offsetY Y偏移量
     * @return 新位置
     */
    public NodePosition offset(double offsetX, double offsetY) {
        return new NodePosition(this.x + offsetX, this.y + offsetY);
    }
}
