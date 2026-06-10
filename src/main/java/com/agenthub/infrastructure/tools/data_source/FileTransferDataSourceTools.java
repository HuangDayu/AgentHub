package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.data_source.dto.FileWriteRequest;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Map;

import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getWorkspace;

@RequiredArgsConstructor
@AgentTools(name = "FileTransferDataSourceTools",
        description = "文件传输数据源工具：对已配置的FTP/SFTP/本地文件系统数据源进行文件读取、写入和列表操作",
        defaultEnable = true)
public class FileTransferDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "读取指定路径的文件内容")
    public DataSourceCommandResult readFile(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "文件路径") String filePath,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("operation", "read", "path", filePath), toolContext);
    }

    @Tool(description = "将内容写入指定路径的文件（覆盖写入）")
    public DataSourceCommandResult writeFile(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "写入请求（文件路径/内容）") FileWriteRequest request,
            ToolContext toolContext) {
        return execute(dataSourceName, Map.of("operation", "write", "path", request.getFilePath(), "content", request.getContent()), toolContext);
    }

    @Tool(description = "列出指定目录下的所有文件和子目录")
    public DataSourceCommandResult listFiles(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "目录路径（留空为根目录）") String directoryPath,
            ToolContext toolContext) {
        String path = directoryPath == null || directoryPath.isBlank() ? "/" : directoryPath;
        return execute(dataSourceName, Map.of("operation", "list", "path", path), toolContext);
    }

    private DataSourceCommandResult execute(String name, Map<String, Object> params, ToolContext ctx) {
        AgentDataSource source = findSource(ctx, name);
        String body = JSONUtil.toJsonStr(params);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), body);
        return toResult(result);
    }

    private AgentDataSource findSource(ToolContext ctx, String name) {
        String workspaceId = getWorkspace(ctx).getWorkspace().getId();
        List<AgentDataSource> sources = repository.findByWorkspaceId(workspaceId);
        return sources.stream()
                .filter(s -> isFileProtocol(s.getProtocol()))
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("文件数据源未找到或已禁用: " + name));
    }

    private static boolean isFileProtocol(AgentDataSourceProtocol p) {
        return p == AgentDataSourceProtocol.FTP || p == AgentDataSourceProtocol.SFTP
                || p == AgentDataSourceProtocol.FILE;
    }

    private DataSourceCommandResult toResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        DataSourceCommandResult res = new DataSourceCommandResult();
        res.setSuccess(r.isSuccess());
        res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        res.setData(r.getData());
        res.setMessage("操作成功，耗时 " + r.getElapsedMs() + "ms");
        return res;
    }
}
