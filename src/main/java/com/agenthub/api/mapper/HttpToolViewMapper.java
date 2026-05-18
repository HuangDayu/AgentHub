package com.agenthub.api.mapper;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.HttpToolInvokeViewResponse;
import com.agenthub.api.dto.HttpToolViewResponse;
import com.agenthub.application.dto.HttpToolInvokeOutput;
import com.agenthub.application.dto.HttpToolOutput;

/**
 * 工具视图映射器。
 */
public final class HttpToolViewMapper {

    private HttpToolViewMapper() {
    }

    public static HttpToolViewResponse toResponse(HttpToolOutput view) {
        return BeanUtil.copyProperties(view, HttpToolViewResponse.class);
    }

    public static HttpToolInvokeViewResponse toResponse(HttpToolInvokeOutput view) {
        return BeanUtil.copyProperties(view, HttpToolInvokeViewResponse.class);
    }
}
