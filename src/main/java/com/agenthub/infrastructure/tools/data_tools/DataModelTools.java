package com.agenthub.infrastructure.tools.data_tools;

import com.agenthub.domain.model.DataFieldMetadata;
import com.agenthub.domain.model.DataModelMetadata;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.agenthub.infrastructure.tools.SystemToolsUtils.getAgentContext;

/**
 * 数据模型通用 CRUD 工具。
 * <p>
 * 提供 6 个固定工具方法，Agent 通过元数据驱动的方式查询和操作数据。
 * </p>
 */
@RequiredArgsConstructor
@AgentTools(name = "DataModelTools", description = "数据模型通用CRUD工具，通过元数据驱动操作数据")
public class DataModelTools {

    private final DataModelScanner scanner;
    private final DataModelInvoker invoker;

    /**
     * 列出所有可操作的数据模型
     */
    @Tool(description = "列出所有可操作的数据模型，返回模型名称、描述和领域")
    public List<Map<String, Object>> listDataModels(ToolContext toolContext) {
        return scanner.getAllModels().stream()
                .map(this::toModelSummary)
                .collect(Collectors.toList());
    }

    /**
     * 查看模型结构
     */
    @Tool(description = "查看数据模型的结构，返回字段列表和操作能力")
    public Map<String, Object> describeDataModel(ToolContext toolContext,
                                                 @ToolParam(description = "数据模型名称（如：知识库、Agent）") String modelName) {
        DataModelMetadata metadata = getModelOrThrow(modelName);
        return toModelDetail(metadata);
    }

    /**
     * 查询数据
     */
    @Tool(description = "查询数据，支持过滤条件和分页")
    public Map<String, Object> queryData(QueryInput input, ToolContext toolContext) {
        DataModelMetadata metadata = getModelOrThrow(input.getModel());
        QueryParams params = buildQueryParams(toolContext, input.getPage(), input.getSize());
        return invoker.query(metadata, input.getFilters(), params);
    }

    /**
     * 创建数据
     */
    @Tool(description = "创建数据")
    public Object createData(
            @ToolParam(description = "数据模型名称") String model,
            @ToolParam(description = "数据内容（JSON对象）") Map<String, Object> data,
            ToolContext toolContext) {
        DataModelMetadata metadata = getModelOrThrow(model);
        validateCreatable(metadata);
        fillContextFields(data, metadata, toolContext);
        return invoker.create(metadata, data);
    }

    /**
     * 更新数据
     */
    @Tool(description = "更新数据")
    public Object updateData(UpdateInput input, ToolContext toolContext) {
        DataModelMetadata metadata = getModelOrThrow(input.getId());
        validateUpdatable(metadata);
        return invoker.update(metadata, input.getId(), input.getData());
    }

    /**
     * 删除数据
     */
    @Tool(description = "删除数据")
    public boolean deleteData(
            @ToolParam(description = "数据模型名称") String model,
            @ToolParam(description = "数据ID") String id,
            ToolContext toolContext) {
        DataModelMetadata metadata = getModelOrThrow(model);
        validateDeletable(metadata);
        return invoker.delete(metadata, id);
    }


    @Tool(description = "批量删除数据")
    public int batchDeleteData(
            @ToolParam(description = "数据模型名称") String model,
            @ToolParam(description = "数据ID集合") List<String> ids,
            ToolContext toolContext) {
        DataModelMetadata metadata = getModelOrThrow(model);
        validateDeletable(metadata);
        return invoker.batchDelete(metadata, ids);
    }

    /**
     * 获取模型或抛出异常
     */
    private DataModelMetadata getModelOrThrow(String modelName) {
        return scanner.getModel(modelName)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + modelName));
    }

    /**
     * 构建查询参数
     */
    private QueryParams buildQueryParams(ToolContext toolContext, Integer page, Integer size) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return QueryParams.builder()
                .tenantId(ctx.getWorkspace().getWorkspace().getTenantId())
                .workspaceId(ctx.getWorkspace().getWorkspace().getId())
                .page(page != null ? page : 1)
                .size(size != null ? Math.min(size, 100) : 20)
                .build();
    }

    /**
     * 验证是否支持创建
     */
    private void validateCreatable(DataModelMetadata metadata) {
        if (!metadata.isCreatable()) {
            throw new RuntimeException("该模型不支持创建操作: " + metadata.getName());
        }
    }

    /**
     * 验证是否支持更新
     */
    private void validateUpdatable(DataModelMetadata metadata) {
        if (!metadata.isUpdatable()) {
            throw new RuntimeException("该模型不支持更新操作: " + metadata.getName());
        }
    }

    /**
     * 验证是否支持删除
     */
    private void validateDeletable(DataModelMetadata metadata) {
        if (!metadata.isDeletable()) {
            throw new RuntimeException("该模型不支持删除操作: " + metadata.getName());
        }
    }

    /**
     * 自动填充上下文字段
     */
    private void fillContextFields(Map<String, Object> data,
                                   DataModelMetadata metadata,
                                   ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        fillTenantField(data, metadata, ctx);
        fillWorkspaceField(data, metadata, ctx);
    }

    /**
     * 填充租户字段
     */
    private void fillTenantField(Map<String, Object> data,
                                 DataModelMetadata metadata,
                                 ReActAgentContext ctx) {
        if (!metadata.getTenantField().isEmpty() && !data.containsKey(metadata.getTenantField())) {
            data.put(metadata.getTenantField(), ctx.getWorkspace().getWorkspace().getTenantId());
        }
    }

    /**
     * 填充工作空间字段
     */
    private void fillWorkspaceField(Map<String, Object> data,
                                    DataModelMetadata metadata,
                                    ReActAgentContext ctx) {
        if (!metadata.getWorkspaceField().isEmpty() && !data.containsKey(metadata.getWorkspaceField())) {
            data.put(metadata.getWorkspaceField(), ctx.getWorkspace().getWorkspace().getId());
        }
    }

    /**
     * 转换为模型摘要
     */
    private Map<String, Object> toModelSummary(DataModelMetadata metadata) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("name", metadata.getName());
        summary.put("description", metadata.getDescription());
        summary.put("domain", metadata.getDomain());
        summary.put("actions", buildActionsList(metadata));
        return summary;
    }

    /**
     * 构建操作列表
     */
    private List<String> buildActionsList(DataModelMetadata metadata) {
        List<String> actions = new ArrayList<>();
        if (metadata.isCreatable()) actions.add("create");
        if (metadata.isUpdatable()) actions.add("update");
        if (metadata.isDeletable()) actions.add("delete");
        return actions;
    }

    /**
     * 转换为模型详情
     */
    private Map<String, Object> toModelDetail(DataModelMetadata metadata) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("name", metadata.getName());
        detail.put("description", metadata.getDescription());
        detail.put("domain", metadata.getDomain());
        detail.put("capabilities", buildCapabilitiesMap(metadata));
        detail.put("fields", buildVisibleFields(metadata));
        return detail;
    }

    /**
     * 构建能力映射
     */
    private Map<String, Boolean> buildCapabilitiesMap(DataModelMetadata metadata) {
        Map<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilities.put("create", metadata.isCreatable());
        capabilities.put("update", metadata.isUpdatable());
        capabilities.put("delete", metadata.isDeletable());
        return capabilities;
    }

    /**
     * 构建可见字段列表
     */
    private List<Map<String, Object>> buildVisibleFields(DataModelMetadata metadata) {
        return metadata.getFields().stream()
                .filter(f -> !f.isHidden())
                .map(this::toFieldSummary)
                .collect(Collectors.toList());
    }

    /**
     * 转换为字段摘要
     */
    private Map<String, Object> toFieldSummary(DataFieldMetadata field) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("name", field.getName());
        summary.put("type", field.getType());
        summary.put("description", field.getDescription());
        summary.put("filterable", field.isFilterable());
        summary.put("required", field.isRequired());
        return summary;
    }
}
