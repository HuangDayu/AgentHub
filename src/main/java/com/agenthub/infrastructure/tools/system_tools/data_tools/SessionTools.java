package com.agenthub.infrastructure.tools.system_tools.data_tools;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import com.agenthub.infrastructure.tools.system_tools.data_tools.dto.AgentSessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 会话数据域工具，提供会话信息查询与创建（不含消息内容）。
 */
@RequiredArgsConstructor
@AgentTools(name = "SessionTools", description = "会话数据工具，提供会话信息查询与创建（不含消息内容）")
public class SessionTools {

    private final SessionRepository sessionRepository;



    @Tool(description = "获取当前Agent的会话列表（不含消息内容）")
    public List<AgentSessionDTO> getSessions(ToolContext toolContext) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        return sessionRepository.findByAgentId(ctx.getAgent().getId()).stream()
                .map(s -> BeanUtil.copyProperties(s, AgentSessionDTO.class))
                .collect(Collectors.toList());
    }

    @Tool(description = "创建新的会话")
    public AgentSessionDTO createSession(ToolContext toolContext,
                                          @ToolParam(description = "会话名称") String name) {
        ReActAgentContext ctx = getAgentContext(toolContext);
        Session session = Session.create(
                ctx.getAgent().getId(),
                name,
                ctx.getAgent().getTenantId(),
                ctx.getWorkspace().getWorkspace().getId());
        return BeanUtil.copyProperties(sessionRepository.save(session), AgentSessionDTO.class);
    }
}
