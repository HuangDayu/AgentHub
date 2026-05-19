package com.agenthub.domain.model.tools;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpToolInvokeResult {
    private /** 工具标识 */ String toolId;
    private /** 调用状态（如 SUCCESS、FAILED） */ String status;
    private /** 调用输出数据 */ Map<String, Object> output;
}
