package com.agenthub.infrastructure.tools.data_tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 更新输入参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInput {
    private String id;
    private Map<String, Object> data;
}
