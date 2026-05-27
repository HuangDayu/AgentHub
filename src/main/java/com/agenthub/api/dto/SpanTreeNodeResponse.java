package com.agenthub.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Span 树节点响应 DTO.
 * 继承 SpanResponse 的所有字段，额外包含子节点列表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpanTreeNodeResponse extends SpanResponse {

    private List<SpanTreeNodeResponse> children = new ArrayList<>();
}
