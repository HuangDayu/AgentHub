package com.agenthub.api.controller;

import com.agenthub.api.dto.AgentDataSourceDescriptorResponse;
import com.agenthub.api.mapper.AgentDataSourceViewMapper;
import com.agenthub.application.port.out.AgentDataSourcePort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据源组件（协议描述符）接口
 * <p>返回 15 种支持协议的元信息、字段、语法提示，供前端动态表单渲染。</p>
 */
@RestController
@RequestMapping("/api/v1/agent-data-source-components")
public class AgentDataSourceComponentController {
    private final AgentDataSourcePort port;

    public AgentDataSourceComponentController(AgentDataSourcePort port) {
        this.port = port;
    }

    /**
     * 列出所有支持的协议描述符
     */
    @GetMapping
    public List<AgentDataSourceDescriptorResponse> listComponents() {
        return port.listDescriptors().stream()
                .map(AgentDataSourceViewMapper::toDescriptorResponse)
                .toList();
    }
}
