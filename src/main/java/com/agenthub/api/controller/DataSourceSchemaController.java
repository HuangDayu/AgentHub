package com.agenthub.api.controller;

import com.agenthub.api.dto.DataSourceSchemaRequest;
import com.agenthub.api.dto.DataSourceSchemaResponse;
import com.agenthub.api.dto.DataSourceTableRequest;
import com.agenthub.api.mapper.AgentDataSourceViewMapper;
import com.agenthub.application.dto.DataSourceSchemaOutput;
import com.agenthub.application.usecase.DataSourceSchemaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源 Schema Controller
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/agent-data-sources/{dataSourceId}/schema")
public class DataSourceSchemaController {
    private final DataSourceSchemaUseCase useCase;

    public DataSourceSchemaController(DataSourceSchemaUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * 获取数据源 schema
     */
    @GetMapping
    public DataSourceSchemaResponse get(@PathVariable String workspaceId, @PathVariable String dataSourceId) {
        return toResponse(useCase.get(dataSourceId));
    }

    /**
     * 整体替换 schema
     */
    @PutMapping
    public DataSourceSchemaResponse replace(@PathVariable String workspaceId,
                                             @PathVariable String dataSourceId,
                                             @RequestBody DataSourceSchemaRequest request) {
        return toResponse(useCase.replace(dataSourceId,
            AgentDataSourceViewMapper.fromSchemaRequest(dataSourceId, request)));
    }

    /**
     * 探测数据源 schema
     */
    @PostMapping("/introspect")
    public DataSourceSchemaResponse introspect(@PathVariable String workspaceId, @PathVariable String dataSourceId) {
        return toResponse(useCase.introspect(dataSourceId));
    }

    /**
     * 新增单表
     */
    @PostMapping("/tables")
    @ResponseStatus(HttpStatus.CREATED)
    public DataSourceSchemaResponse addTable(@PathVariable String workspaceId,
                                              @PathVariable String dataSourceId,
                                              @RequestBody DataSourceTableRequest request) {
        return toResponse(useCase.addTable(dataSourceId, AgentDataSourceViewMapper.fromTableRequest(request)));
    }

    /**
     * 删除单表
     */
    @DeleteMapping("/tables/{tableId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(@PathVariable String workspaceId,
                            @PathVariable String dataSourceId,
                            @PathVariable String tableId) {
        useCase.deleteTable(dataSourceId, tableId);
    }

    /**
     * Output → Response 转换
     */
    private DataSourceSchemaResponse toResponse(DataSourceSchemaOutput output) {
        return AgentDataSourceViewMapper.toSchemaResponse(output);
    }
}
