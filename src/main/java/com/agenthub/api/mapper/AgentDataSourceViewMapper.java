package com.agenthub.api.mapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.agenthub.api.dto.AgentDataSourceDescriptorResponse;
import com.agenthub.api.dto.AgentDataSourceFieldResponse;
import com.agenthub.api.dto.AgentDataSourceInvokeResponse;
import com.agenthub.api.dto.AgentDataSourceResponse;
import com.agenthub.api.dto.AgentDataSourceTestResponse;
import com.agenthub.api.dto.AuditEventResponse;
import com.agenthub.api.dto.DataSourceColumnRequest;
import com.agenthub.api.dto.DataSourceColumnResponse;
import com.agenthub.api.dto.DataSourceSchemaRequest;
import com.agenthub.api.dto.DataSourceSchemaResponse;
import com.agenthub.api.dto.DataSourceTableRequest;
import com.agenthub.api.dto.DataSourceTableResponse;
import com.agenthub.api.dto.PermissionStrategyRequest;
import com.agenthub.api.dto.PermissionStrategyResponse;
import com.agenthub.api.dto.TableRelationshipRequest;
import com.agenthub.api.dto.TableRelationshipResponse;
import com.agenthub.application.command.UpsertPermissionStrategyCommand;
import com.agenthub.application.dto.AgentDataSourceOutput;
import com.agenthub.application.dto.AuditEventOutput;
import com.agenthub.application.dto.DataSourceColumnOutput;
import com.agenthub.application.dto.DataSourceSchemaOutput;
import com.agenthub.application.dto.DataSourceTableOutput;
import com.agenthub.application.dto.PermissionStrategyOutput;
import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.enums.OperationLevel;
import com.agenthub.domain.enums.TableOperation;
import com.agenthub.domain.model.AgentDataSourceDescriptor;
import com.agenthub.domain.model.AgentDataSourceField;
import com.agenthub.domain.model.DataSourceColumn;
import com.agenthub.domain.model.DataSourceSchema;
import com.agenthub.domain.model.DataSourceTable;
import com.agenthub.domain.model.TableRelationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Agent 数据源/审计/Schema 视图转换器
 * <p>Controller 通过此类完成 Request/Output/Domain Model 之间的转换，避免 Controller 直接依赖 domain 模型。</p>
 */
public class AgentDataSourceViewMapper {

    public static AgentDataSourceResponse toResponse(AgentDataSourceOutput output) {
        if (output == null) return null;
        AgentDataSourceResponse resp = new AgentDataSourceResponse();
        BeanUtil.copyProperties(output, resp);
        return resp;
    }

    public static AgentDataSourceTestResponse toTestResponse(AgentDataSourcePort.AgentDataSourceTestResult result) {
        if (result == null) return null;
        return new AgentDataSourceTestResponse(result.isSuccess(), result.getElapsedMs(), result.getMessage());
    }

    public static AgentDataSourceInvokeResponse toInvokeResponse(AgentDataSourcePort.AgentDataSourceInvokeResult result) {
        if (result == null) return null;
        return new AgentDataSourceInvokeResponse(
            result.isSuccess(), result.getData(), result.getElapsedMs(),
            result.getExchangeId(), result.getErrorMessage()
        );
    }

    public static AgentDataSourceDescriptorResponse toDescriptorResponse(AgentDataSourceDescriptor descriptor) {
        if (descriptor == null) return null;
        AgentDataSourceDescriptorResponse resp = new AgentDataSourceDescriptorResponse();
        BeanUtil.copyProperties(descriptor, resp, CopyOptions.create().setIgnoreProperties("fields"));
        resp.setFields(toFieldResponses(descriptor.getFields()));
        return resp;
    }

    public static List<AgentDataSourceFieldResponse> toFieldResponses(List<AgentDataSourceField> fields) {
        if (fields == null) return List.of();
        List<AgentDataSourceFieldResponse> out = new ArrayList<>(fields.size());
        for (AgentDataSourceField f : fields) {
            out.add(toFieldResponse(f));
        }
        return out;
    }

    public static AgentDataSourceFieldResponse toFieldResponse(AgentDataSourceField field) {
        if (field == null) return null;
        AgentDataSourceFieldResponse resp = new AgentDataSourceFieldResponse();
        BeanUtil.copyProperties(field, resp);
        return resp;
    }

    public static DataSourceSchemaResponse toSchemaResponse(DataSourceSchemaOutput output) {
        if (output == null) return null;
        DataSourceSchemaResponse resp = new DataSourceSchemaResponse();
        BeanUtil.copyProperties(output, resp);
        resp.setTables(toTableResponsesFromOutput(output.getTables()));
        return resp;
    }

    public static List<DataSourceTableResponse> toTableResponsesFromOutput(List<DataSourceTableOutput> tables) {
        if (tables == null) return List.of();
        List<DataSourceTableResponse> out = new ArrayList<>(tables.size());
        for (DataSourceTableOutput t : tables) {
            out.add(toTableResponse(t));
        }
        return out;
    }

    public static DataSourceTableResponse toTableResponse(DataSourceTableOutput t) {
        if (t == null) return null;
        DataSourceTableResponse resp = new DataSourceTableResponse();
        BeanUtil.copyProperties(t, resp);
        resp.setColumns(toColumnResponsesFromOutput(t.getColumns()));
        return resp;
    }

    public static List<DataSourceColumnResponse> toColumnResponsesFromOutput(List<DataSourceColumnOutput> columns) {
        if (columns == null) return List.of();
        List<DataSourceColumnResponse> out = new ArrayList<>(columns.size());
        for (DataSourceColumnOutput c : columns) {
            out.add(toColumnResponse(c));
        }
        return out;
    }

    public static DataSourceColumnResponse toColumnResponse(DataSourceColumnOutput c) {
        if (c == null) return null;
        DataSourceColumnResponse resp = new DataSourceColumnResponse();
        BeanUtil.copyProperties(c, resp);
        return resp;
    }

    public static PermissionStrategyResponse toResponse(PermissionStrategyOutput output) {
        if (output == null) return null;
        PermissionStrategyResponse resp = new PermissionStrategyResponse();
        BeanUtil.copyProperties(output, resp);
        return resp;
    }

    public static AuditEventResponse toResponse(AuditEventOutput output) {
        if (output == null) return null;
        AuditEventResponse resp = new AuditEventResponse();
        BeanUtil.copyProperties(output, resp);
        return resp;
    }

    public static UpsertPermissionStrategyCommand toCommand(PermissionStrategyRequest request) {
        if (request == null) return null;
        UpsertPermissionStrategyCommand cmd = new UpsertPermissionStrategyCommand();
        setBaseFields(cmd, request);
        setPermissionFields(cmd, request);
        setRateLimitFields(cmd, request);
        return cmd;
    }

    private static void setBaseFields(UpsertPermissionStrategyCommand cmd, PermissionStrategyRequest request) {
        cmd.setName(request.getName());
        cmd.setDescription(request.getDescription());
    }

    private static void setPermissionFields(UpsertPermissionStrategyCommand cmd, PermissionStrategyRequest request) {
        cmd.setAllowedRoles(request.getAllowedRoles() == null ? Set.of() : new HashSet<>(request.getAllowedRoles()));
        cmd.setAllowedOperations(toOperationLevels(request.getAllowedOperations()));
        cmd.setProtocolBlocklist(toProtocolBlocklist(request.getProtocolBlocklist()));
        cmd.setDangerousSqlBlock(Boolean.TRUE.equals(request.getDangerousSqlBlock()));
        cmd.setRequireApprovalFor(toTableOps(request.getRequireApprovalFor()));
        cmd.setTablePermissions(toTablePermissionMap(request.getTablePermissions()));
        cmd.setPiiMaskingOnResult(Boolean.TRUE.equals(request.getPiiMaskingOnResult()));
    }

    private static void setRateLimitFields(UpsertPermissionStrategyCommand cmd, PermissionStrategyRequest request) {
        cmd.setRateLimitPerMinute(request.getRateLimitPerMinute() == null ? 0 : request.getRateLimitPerMinute());
        cmd.setRateLimitPerHour(request.getRateLimitPerHour() == null ? 0 : request.getRateLimitPerHour());
    }

    public static DataSourceSchema fromSchemaRequest(String dataSourceId, DataSourceSchemaRequest request) {
        if (request == null) return null;
        DataSourceSchema schema = new DataSourceSchema();
        schema.setDataSourceId(dataSourceId);
        schema.setTables(fromTableRequests(request.getTables()));
        return schema;
    }

    public static DataSourceTable fromTableRequest(DataSourceTableRequest request) {
        if (request == null) return null;
        DataSourceTable t = new DataSourceTable();
        t.setId(request.getId());
        t.setName(request.getName());
        t.setDisplayName(request.getDisplayName());
        t.setDescription(request.getDescription());
        t.setColumns(fromColumnRequests(request.getColumns()));
        return t;
    }

    public static List<DataSourceTable> fromTableRequests(List<DataSourceTableRequest> requests) {
        if (requests == null) return List.of();
        List<DataSourceTable> out = new ArrayList<>(requests.size());
        for (DataSourceTableRequest r : requests) {
            out.add(fromTableRequest(r));
        }
        return out;
    }

    public static DataSourceColumn fromColumnRequest(DataSourceColumnRequest request) {
        if (request == null) return null;
        DataSourceColumn c = new DataSourceColumn();
        BeanUtil.copyProperties(request, c, CopyOptions.create().setIgnoreProperties("nullable"));
        c.setNullable(Boolean.TRUE.equals(request.getNullable()));
        c.setPrimary(Boolean.TRUE.equals(request.getPrimaryKey()));
        return c;
    }

    public static List<DataSourceColumn> fromColumnRequests(List<DataSourceColumnRequest> requests) {
        if (requests == null) return List.of();
        List<DataSourceColumn> out = new ArrayList<>(requests.size());
        for (DataSourceColumnRequest r : requests) {
            out.add(fromColumnRequest(r));
        }
        return out;
    }

    public static TableRelationship fromRelRequest(TableRelationshipRequest request) {
        if (request == null) return null;
        TableRelationship r = new TableRelationship();
        BeanUtil.copyProperties(request, r);
        return r;
    }

    public static List<TableRelationship> fromRelRequests(List<TableRelationshipRequest> requests) {
        if (requests == null) return List.of();
        List<TableRelationship> out = new ArrayList<>(requests.size());
        for (TableRelationshipRequest r : requests) {
            out.add(fromRelRequest(r));
        }
        return out;
    }

    private static Set<OperationLevel> toOperationLevels(Set<String> raw) {
        return toEnumSet(raw, OperationLevel::valueOf);
    }

    private static Set<AgentDataSourceProtocol> toProtocolBlocklist(Set<String> raw) {
        return toEnumSet(raw, AgentDataSourceProtocol::valueOf);
    }

    private static Set<TableOperation> toTableOps(Set<String> raw) {
        return toEnumSet(raw, TableOperation::valueOf);
    }

    private static <T extends Enum<T>> Set<T> toEnumSet(Set<String> raw, Function<String, T> mapper) {
        if (raw == null) return Set.of();
        Set<T> out = new HashSet<>();
        for (String s : raw) {
            if (s == null || s.isBlank()) continue;
            try {
                out.add(mapper.apply(s.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    private static java.util.Map<String, Set<TableOperation>> toTablePermissionMap(
            java.util.Map<String, Set<String>> raw) {
        if (raw == null) return java.util.Map.of();
        java.util.Map<String, Set<TableOperation>> out = new java.util.HashMap<>();
        raw.forEach((k, v) -> out.put(k, toTableOps(v)));
        return out;
    }

    private static String name(Enum<?> e) {
        return e == null ? null : e.name();
    }
}
