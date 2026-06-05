package com.agenthub.infrastructure.tools.system_tools;

import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.application.port.out.tools.SystemToolScannerPort;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.AgentToolInfo;
import com.agenthub.domain.model.tools.SystemTool;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Map.entry;

/**
 * @author huangdayu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemToolsFactory implements AbstractToolsFactory, SystemToolScannerPort {

    private final ApplicationContext applicationContext;
    private final SystemToolsRepository systemToolsRepository;

    private static final Map<Object, SystemTool> SYSTEM_TOOLS = new ConcurrentHashMap<>();

    private static final Map<Object, ToolCallback[]> TOOL_CALLBACKS = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(AgentTools.class);
        beansWithAnnotation.values().stream().filter(v -> hasTools(AopUtils.isAopProxy(v) ? ClassUtils.getUserClass(v) : v.getClass())).forEach(v -> {
            MethodToolCallbackProvider callbackProvider = MethodToolCallbackProvider.builder().toolObjects(v).build();
            TOOL_CALLBACKS.put(v, callbackProvider.getToolCallbacks());
            SYSTEM_TOOLS.put(v, createTool(v));
        });
    }

    private boolean hasTools(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        methods.addAll(List.of(clazz.getMethods()));
        methods.addAll(List.of(clazz.getDeclaredMethods()));
        return !methods.isEmpty() && methods.stream().anyMatch(m -> null != m.getAnnotation(Tool.class));
    }


    public void addToolCallback(Object bean, ToolCallback[] toolCallbacks) {
        if (toolCallbacks != null && toolCallbacks.length > 0) {
            TOOL_CALLBACKS.put(bean, toolCallbacks);
        }
    }

    @Override
    public List<SystemTool> scanSystemTools() {
        return SYSTEM_TOOLS.values().stream().toList();
    }

    @Override
    public AgentToolType getToolInfo() {
        return AgentToolType.SYSTEM_TOOL;
    }

    public Set<ToolCallback> getAllToolCallbacks() {
        return TOOL_CALLBACKS.values().stream().flatMap(Arrays::stream).collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(String name) {
        return getAllToolCallbacks().stream()
                .filter(toolCallback -> ClassUtils.getUserClass(toolCallback.getClass()).getSimpleName().equals(name) ||
                        toolCallback.getToolDefinition().name().equals(name))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(List<AgentToolInfo> toolIds) {
        if (toolIds.isEmpty()) {
            return Set.of();
        }
        Set<String> collect1 = toolIds.parallelStream().map(AgentToolInfo::getName).collect(Collectors.toSet());
        Set<Object> collect2 = SYSTEM_TOOLS.keySet().parallelStream()
                .filter(o -> collect1.contains(ClassUtils.getUserClass(o).getSimpleName()))
                .collect(Collectors.toSet());
        return collect2.parallelStream().flatMap(o -> Arrays.stream(TOOL_CALLBACKS.get(o))).collect(Collectors.toSet());
    }

    public void clearToolCallbacks() {
        TOOL_CALLBACKS.clear();
    }


    private SystemTool createTool(Object bean) {
        Class<?> userClass = ClassUtils.getUserClass(bean);
        AgentTools agentTools = userClass.getAnnotation(AgentTools.class);
        SystemTool.CreationSpec request = new SystemTool.CreationSpec(
                userClass.getName(),
                userClass.getSimpleName(),
                agentTools.description(),
                extractCategory(userClass),
                countToolMethods(userClass),
                agentTools.defaultEnable()
        );
        return SystemTool.create(request);
    }

    private static final Map<String, String> CATEGORY_MAP = Map.ofEntries(
            entry("Runtime", "runtime"), entry("Fs", "filesystem"),
            entry("Session", "session"), entry("Memory", "memory"),
            entry("Web", "web"), entry("Browser", "browser"),
            entry("Automation", "automation"), entry("Node", "node"),
            entry("Media", "media"), entry("Agent", "agent")
    );

    private String extractCategory(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return CATEGORY_MAP.entrySet().stream()
                .filter(e -> name.contains(e.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse("system");
    }

    private int countToolMethods(Class<?> clazz) {
        return (int) java.util.Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .count();
    }

}
