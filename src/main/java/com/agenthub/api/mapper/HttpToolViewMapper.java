package com.agenthub.api.mapper;

import com.agenthub.api.dto.HttpToolInvokeViewResponse;
import com.agenthub.api.dto.HttpToolViewResponse;
import com.agenthub.domain.model.HttpToolInvokeView;
import com.agenthub.domain.model.HttpToolView;

/**
 * 工具视图映射器。
 */
public final class HttpToolViewMapper {

    private HttpToolViewMapper() {
    }

    public static HttpToolViewResponse toResponse(HttpToolView view) {
        return new HttpToolViewResponse(
            view.id(),
            view.name(),
            view.description(),
            view.enabled()
        );
    }

    public static HttpToolInvokeViewResponse toResponse(HttpToolInvokeView view) {
        return new HttpToolInvokeViewResponse(
            view.toolId(),
            view.status(),
            view.output()
        );
    }
}
