package com.agenthub.application.port.out;

import com.agenthub.application.command.InvokeToolCommand;
import com.agenthub.domain.model.HttpTool;
import com.agenthub.domain.model.HttpToolInvokeResult;

import java.util.Map;

/**
 * 工具调用器接口，定义工具执行能力。
 * <p>
 * 实现类负责根据工具配置调用外部服务并返回结果。
 */
public interface HttpToolInvoker {

    HttpToolInvokeResult invoke(String toolId, InvokeToolCommand command);

    /**
     * 调用工具执行。
     *
     * @param httpTool    工具配置
     * @param payload 调用参数
     * @return 工具调用结果
     */
    HttpToolInvokeResult invoke(HttpTool httpTool, Map<String, Object> payload);
}
