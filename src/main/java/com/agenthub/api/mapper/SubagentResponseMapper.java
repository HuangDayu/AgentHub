package com.agenthub.api.mapper;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.SubagentResponse;
import com.agenthub.application.dto.SubagentOutput;

/**
 * 子智能体响应映射器，将输出DTO转换为响应DTO。
 */
public class SubagentResponseMapper {

    /**
     * 将输出DTO转换为响应DTO。
     *
     * @param output 子Agent输出DTO
     * @return 子Agent响应DTO
     */
    public static SubagentResponse toResponse(SubagentOutput output) {
        return BeanUtil.copyProperties(output, SubagentResponse.class);
    }
}
