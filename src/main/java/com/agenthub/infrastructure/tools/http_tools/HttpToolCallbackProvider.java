package com.agenthub.infrastructure.tools.http_tools;

import com.agenthub.application.port.out.repositories.HttpToolRepository;
import com.agenthub.domain.model.HttpTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP工具回调提供者，将HttpTool配置转换为ToolCallback。
 * 
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpToolCallbackProvider {
    
    private final HttpToolRepository httpToolRepository;
    private final ObjectMapper objectMapper;
    
    @Qualifier("tool.restTemplate")
    private final RestTemplate restTemplate;
    
    /**
     * 获取所有HTTP工具的ToolCallback。
     */
    public Set<ToolCallback> getToolCallbacks() {
        List<HttpTool> httpTools = httpToolRepository.findAll();
        return convertToToolCallbacks(httpTools);
    }
    
    /**
     * 将HttpTool列表转换为ToolCallback集合。
     */
    private Set<ToolCallback> convertToToolCallbacks(List<HttpTool> httpTools) {
        return httpTools.stream()
                .filter(this::isEnabled)
                .map(this::createToolCallback)
                .collect(Collectors.toSet());
    }
    
    /**
     * 判断HTTP工具是否启用。
     */
    private boolean isEnabled(HttpTool httpTool) {
        return httpTool.enabled();
    }
    
    /**
     * 创建ToolCallback。
     */
    private ToolCallback createToolCallback(HttpTool httpTool) {
        log.debug("Creating ToolCallback for HTTP tool: {}", httpTool.name());
        return new HttpToolCallback(httpTool, restTemplate, objectMapper);
    }
}
