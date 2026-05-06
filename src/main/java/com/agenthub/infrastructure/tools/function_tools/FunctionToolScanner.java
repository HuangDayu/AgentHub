package com.agenthub.infrastructure.tools.function_tools;

import com.agenthub.application.port.out.tools.FunctionToolScannerPort;
import com.agenthub.domain.model.FunctionTool;
import com.agenthub.infrastructure.tools.function_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FunctionToolScanner implements FunctionToolScannerPort {

    private final ApplicationContext applicationContext;

    @Override
    public List<FunctionTool> scanFunctionTools() {
        List<FunctionTool> tools = new ArrayList<>();
        Map<String, Object> beans = getToolBeans();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            tools.add(createTool(entry.getValue()));
        }
        return tools;
    }

    private Map<String, Object> getToolBeans() {
        return applicationContext.getBeansWithAnnotation(AgentTools.class);
    }

    private FunctionTool createTool(Object bean) {
        return FunctionTool.create(
            bean.getClass().getName(),
            bean.getClass().getSimpleName(),
            "System tool: " + bean.getClass().getSimpleName(),
            extractCategory(bean),
            countToolMethods(bean)
        );
    }

    private String extractCategory(Object bean) {
        String name = bean.getClass().getSimpleName();
        if (name.contains("Runtime")) return "runtime";
        if (name.contains("Fs")) return "filesystem";
        if (name.contains("Session")) return "session";
        if (name.contains("Memory")) return "memory";
        if (name.contains("Web")) return "web";
        if (name.contains("Browser")) return "browser";
        if (name.contains("Automation")) return "automation";
        if (name.contains("Node")) return "node";
        if (name.contains("Media")) return "media";
        if (name.contains("Agent")) return "agent";
        return "system";
    }

    private int countToolMethods(Object bean) {
        return (int) java.util.Arrays.stream(bean.getClass().getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(Tool.class))
            .count();
    }
}
