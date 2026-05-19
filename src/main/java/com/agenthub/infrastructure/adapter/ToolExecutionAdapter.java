package com.agenthub.infrastructure.adapter;

import com.agenthub.application.port.out.tools.ToolExecutionPort;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工具执行适配器。
 * 提供工具执行的默认实现。
 *
 * @author huangdayu
 */
@Component
public class ToolExecutionAdapter implements ToolExecutionPort {

    @Override
    public Object execute(String toolName, Map<String, Object> parameters) {
        // 默认实现：返回工具名称和参数
        return Map.of(
            "toolName", toolName,
            "parameters", parameters,
            "result", "Tool execution completed"
        );
    }
}
