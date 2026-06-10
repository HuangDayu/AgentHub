package com.agenthub.infrastructure.tools.data_source;

import com.agenthub.application.port.out.AgentDataSourcePort;
import com.agenthub.application.port.out.repositories.AgentDataSourceRepository;
import com.agenthub.domain.enums.AgentDataSourceProtocol;
import com.agenthub.domain.exception.ValidationException;
import com.agenthub.domain.model.AgentDataSource;
import com.agenthub.infrastructure.tools.data_source.dto.DataSourceCommandResult;
import com.agenthub.infrastructure.tools.data_source.dto.MailSendRequest;
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
@AgentTools(name = "MailDataSourceTools",
        description = "邮件数据源工具：通过已配置的邮件数据源发送邮件",
        defaultEnable = true)
public class MailDataSourceTools {

    private final AgentDataSourceRepository repository;
    private final AgentDataSourcePort port;

    @Tool(description = "发送邮件。返回发送状态。")
    public DataSourceCommandResult sendMail(
            @ToolParam(description = "数据源名称（通过listAgentDataSources获取）") String dataSourceName,
            @ToolParam(description = "邮件发送请求（收件人/主题/正文/抄送）") MailSendRequest request,
            ToolContext toolContext) {
        AgentDataSource source = findSource(toolContext, dataSourceName);
        Map<String, Object> params = Map.of(
                "to", request.getTo(), "subject", request.getSubject(),
                "body", request.getBody(), "cc", request.getCc() != null ? request.getCc() : "");
        String json = JSONUtil.toJsonStr(params);
        AgentDataSourcePort.AgentDataSourceInvokeResult result = port.invoke(source, Map.of(), json);
        return toResult(result);
    }

    private AgentDataSource findSource(ToolContext ctx, String name) {
        String workspaceId = getWorkspace(ctx).getWorkspace().getId();
        List<AgentDataSource> sources = repository.findByWorkspaceId(workspaceId);
        return sources.stream()
                .filter(s -> s.getProtocol() == AgentDataSourceProtocol.MAIL)
                .filter(s -> s.getName().equals(name) && s.isEnabled())
                .findFirst()
                .orElseThrow(() -> new ValidationException("邮件数据源未找到或已禁用: " + name));
    }

    private DataSourceCommandResult toResult(AgentDataSourcePort.AgentDataSourceInvokeResult r) {
        DataSourceCommandResult res = new DataSourceCommandResult();
        res.setSuccess(r.isSuccess());
        res.setElapsedMs(r.getElapsedMs());
        if (!r.isSuccess()) { res.setErrorMessage(r.getErrorMessage()); return res; }
        res.setData(r.getData());
        res.setMessage("邮件发送成功，耗时 " + r.getElapsedMs() + "ms");
        return res;
    }
}
