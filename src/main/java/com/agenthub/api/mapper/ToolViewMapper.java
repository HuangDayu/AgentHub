package com.agenthub.api.mapper;

import com.agenthub.api.dto.ToolInvokeViewResponse;
import com.agenthub.api.dto.ToolViewResponse;
import com.agenthub.domain.model.ToolInvokeView;
import com.agenthub.domain.model.ToolView;

/**
 * 工具视图映射器。
 */
public final class ToolViewMapper {

    private ToolViewMapper() {
    }

    public static ToolViewResponse toResponse(ToolView view) {
        return new ToolViewResponse(
            view.id(),
            view.name(),
            view.description(),
            view.enabled()
        );
    }

    public static ToolInvokeViewResponse toResponse(ToolInvokeView view) {
        return new ToolInvokeViewResponse(
            view.toolId(),
            view.status(),
            view.output()
        );
    }
}
