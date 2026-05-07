package com.agenthub.infrastructure.tools.system_tools;

import cn.hutool.core.collection.CollUtil;
import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.domain.model.AgentToolInfo;
import com.agenthub.domain.model.SystemTool;
import com.agenthub.infrastructure.tools.AbstractToolsFactory;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static com.agenthub.domain.model.AgentToolType.SYSTEM_TOOLS;

/**
 * @author huangdayu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemToolsFactory implements AbstractToolsFactory {

    private final SystemToolScanner systemToolScanner;
    private final SystemToolsRepository systemToolsRepository;

    @Getter
    private final List<SystemTool> systemTools = new LinkedList<>();

    private static final Set<ToolCallback> TOOL_CALLBACKS = new CopyOnWriteArraySet<>();


    @PostConstruct
    public void init() {
        systemTools.addAll(systemToolScanner.scanSystemTools());
    }

    public void addToolCallback(List<ToolCallback> toolCallbacks) {
        if (CollUtil.isNotEmpty(toolCallbacks)) {
            TOOL_CALLBACKS.addAll(toolCallbacks);
        }
    }

    public void removeToolCallback(List<ToolCallback> toolCallbacks) {
        if (CollUtil.isNotEmpty(toolCallbacks)) {
            toolCallbacks.forEach(TOOL_CALLBACKS::remove);
        }
    }

    @Override
    public AgentToolInfo getToolInfo() {
        return new AgentToolInfo(SYSTEM_TOOLS);
    }

    public Set<ToolCallback> getAllToolCallbacks() {
        return TOOL_CALLBACKS;
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(String name) {
        return getAllToolCallbacks().stream()
                .filter(toolCallback -> toolCallback.getClass().getSimpleName().equals(name))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ToolCallback> getToolCallbacks(List<String> toolIds) {
        List<SystemTool> systemTools = systemToolsRepository.findByIds(toolIds);
        Set<String> collect = systemTools.stream().map(SystemTool::getToolClassName).collect(Collectors.toSet());
        return getAllToolCallbacks().stream().filter(toolCallback -> collect.contains(toolCallback.getClass().getName())).collect(Collectors.toSet());
    }

    public void clearToolCallbacks() {
        TOOL_CALLBACKS.clear();
    }

}
