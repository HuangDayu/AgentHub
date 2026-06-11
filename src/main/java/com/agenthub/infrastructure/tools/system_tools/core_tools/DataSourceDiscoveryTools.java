package com.agenthub.infrastructure.tools.system_tools.core_tools;

import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.application.port.out.repositories.DataSourceSchemaRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.domain.model.DataSourceColumn;
import com.agenthub.domain.model.DataSourceSchema;
import com.agenthub.domain.model.DataSourceTable;
import com.agenthub.infrastructure.tools.data_source.standard.ProtocolStandard;
import com.agenthub.infrastructure.tools.data_source.standard.ProtocolStandardRegistry;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Optional;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "DataSourceDiscoveryTools",
        description = "数据源发现工具：查询可用数据源列表、schema 结构和协议规范",
        defaultEnable = true)
public class DataSourceDiscoveryTools {

    private final AgentDataSourceRepository dataSourceRepository;
    private final DataSourceSchemaRepository schemaRepository;
    private final ProtocolStandardRegistry standardRegistry;

    @Tool(description = "列出当前工作空间下所有已启用的数据源")
    public String listAgentDataSources(ToolContext toolContext) {
        String workspaceId = getWorkspace(toolContext).getWorkspace().getId();
        List<AgentDataSource> sources = dataSourceRepository.findByWorkspaceId(workspaceId).stream()
                .filter(AgentDataSource::isEnabled).toList();
        if (sources.isEmpty()) return "当前工作空间没有已启用的数据源";
        return buildSourceList(sources);
    }

    @Tool(description = "获取指定数据源的 schema 信息，包括表和列定义")
    public String describeDataSourceSchema(
            @ToolParam(description = "数据源名称") String dataSourceName,
            ToolContext toolContext) {
        String workspaceId = getWorkspace(toolContext).getWorkspace().getId();
        var source = dataSourceRepository.findByWorkspaceId(workspaceId).stream()
                .filter(s -> s.getName().equals(dataSourceName) && s.isEnabled()).findFirst();
        if (source.isEmpty()) return "未找到数据源: " + dataSourceName;
        var schema = schemaRepository.findByDataSourceId(source.get().getId());
        if (schema.isEmpty() || isEmptySchema(schema.get()))
            return "数据源 [" + dataSourceName + "] 没有 schema 信息，请先执行 schema 发现";
        return buildSchemaDetail(dataSourceName, schema.get().getTables());
    }

    @Tool(description = "获取指定协议的完整使用规范，包括语法规则、支持的操作、示例、错误处理和最佳实践。"
        + "在调用任何数据源工具之前，建议先通过此工具了解协议的使用标准。")
    public String describeProtocolStandard(
            @ToolParam(description = "协议名称，如 JDBC/HTTP/MONGODB/REDIS/KAFKA/FTP/SFTP/FILE/MAIL/JMS") String protocol,
            ToolContext toolContext) {
        AgentDataSourceProtocol proto = parseProtocol(protocol);
        if (proto == null) {
            return "不支持的协议: " + protocol + "。支持的协议: JDBC, HTTP, HTTPS, REST, MONGODB, REDIS, KAFKA, FTP, SFTP, FILE, MAIL, JMS";
        }
        ProtocolStandard standard = standardRegistry.get(proto);
        if (standard == null) return "协议 [" + protocol + "] 暂无可用的规范文档";
        return buildStandardDetail(standard);
    }

    private String buildStandardDetail(ProtocolStandard s) {
        StringBuilder sb = new StringBuilder();
        sb.append("协议: ").append(s.getDisplayName()).append("\n");
        sb.append("---\n");
        sb.append(s.getDescription()).append("\n\n");
        return appendStandardSections(sb, s);
    }

    private static String appendStandardSections(StringBuilder sb, ProtocolStandard s) {
        sb.append("【语法规则】\n").append(s.getSyntaxGuidelines()).append("\n\n");
        sb.append("【支持的操作】\n").append(s.getAllowedOperations()).append("\n\n");
        sb.append("【示例】\n").append(s.getExamples()).append("\n\n");
        sb.append("【错误处理】\n").append(s.getErrorHandling()).append("\n\n");
        sb.append("【安全注意事项】\n").append(s.getSecurityNotes()).append("\n\n");
        sb.append("【最佳实践】\n").append(s.getBestPractices());
        return sb.toString();
    }

    private static AgentDataSourceProtocol parseProtocol(String name) {
        if (name == null) return null;
        String upper = name.toUpperCase();
        for (AgentDataSourceProtocol p : AgentDataSourceProtocol.values()) {
            if (p.name().equals(upper) || p.getDisplayName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    private boolean isEmptySchema(DataSourceSchema schema) {
        return schema.getTables() == null || schema.getTables().isEmpty();
    }

    private String buildSourceList(List<AgentDataSource> sources) {
        StringBuilder sb = new StringBuilder("可用数据源:\n");
        for (AgentDataSource s : sources) {
            sb.append("- ").append(s.getName())
              .append(" [协议: ").append(protocolName(s.getProtocol()))
              .append(", URI: ").append(safe(s.getEndpointUri()))
              .append("] ").append(safe(s.getDescription())).append("\n");
        }
        return sb.toString();
    }

    private String buildSchemaDetail(String name, List<DataSourceTable> tables) {
        StringBuilder sb = new StringBuilder("数据源 [").append(name).append("] Schema:\n\n");
        appendTableList(sb, tables);
        return sb.toString();
    }

    private void appendTableList(StringBuilder sb, List<DataSourceTable> tables) {
        for (var table : tables) {
            sb.append("表: ").append(table.getName());
            if (table.getDescription() != null && !table.getDescription().isEmpty()) {
                sb.append(" (").append(table.getDescription()).append(")");
            }
            sb.append("\n");
            appendColumnList(sb, table.getColumns());
        }
    }

    private void appendColumnList(StringBuilder sb, List<DataSourceColumn> columns) {
        if (columns == null || columns.isEmpty()) return;
        for (var col : columns) {
            sb.append("  - ").append(col.getName()).append(" (").append(col.getType()).append(")");
            if (col.isPrimary()) sb.append(" [主键]");
            if (col.getDescription() != null) sb.append(" ").append(col.getDescription());
            sb.append("\n");
        }
        sb.append("\n");
    }

    private static String protocolName(AgentDataSourceProtocol protocol) {
        return protocol != null ? protocol.name() : "UNKNOWN";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}