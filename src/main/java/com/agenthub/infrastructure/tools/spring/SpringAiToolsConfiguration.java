package com.agenthub.infrastructure.tools.spring;

import com.agenthub.infrastructure.tools.annotations.AgentTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huangdayu
 */
@Configuration
public class SpringAiToolsConfiguration {


    /**
     * 自动注册容器中所有注解了@ThingsTools的Bean的Tools
     *
     * @param applicationContext
     * @return
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(ApplicationContext applicationContext, AgentToolsFactory agentToolsFactory) {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(AgentTools.class);
        Object[] array = beansWithAnnotation.values().stream().filter(v -> hasTools(v.getClass())).toArray();
        MethodToolCallbackProvider callbackProvider = MethodToolCallbackProvider.builder().toolObjects(array).build();
        agentToolsFactory.addToolCallback(List.of(callbackProvider.getToolCallbacks()));
        return callbackProvider;
    }

    private boolean hasTools(Class<?> clazz) {
        if (!clazz.getAnnotation(AgentTools.class).defaultEnable()) {
            return false;
        }
        List<Method> methods = new ArrayList<>();
        methods.addAll(List.of(clazz.getMethods()));
        methods.addAll(List.of(clazz.getDeclaredMethods()));
        return !methods.isEmpty() && methods.stream().anyMatch(m -> null != m.getAnnotation(Tool.class));
    }

}
