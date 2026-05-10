package com.agenthub.domain.model;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpToolInvokeView {
    private /** 工具唯一标识 */ String toolId;
    private /** 调用状态 */ String status;
    private /** 调用输出数据 */ Map<String, Object> output;
}
