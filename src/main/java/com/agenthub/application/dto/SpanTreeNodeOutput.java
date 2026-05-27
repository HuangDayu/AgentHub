package com.agenthub.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Span 树节点输出 DTO.
 * 继承 SpanOutput 的所有字段，额外包含子节点列表。
 * 用于 RuntimeDataViewUseCase 返回嵌套的树结构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpanTreeNodeOutput extends SpanOutput {

    private List<SpanTreeNodeOutput> children = new ArrayList<>();

    public SpanTreeNodeOutput() {
        super();
    }

    public static SpanTreeNodeOutput from(SpanOutput span) {
        SpanTreeNodeOutput node = new SpanTreeNodeOutput();
        cn.hutool.core.bean.BeanUtil.copyProperties(span, node);
        return node;
    }

}
