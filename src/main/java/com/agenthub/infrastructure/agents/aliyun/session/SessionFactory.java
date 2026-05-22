package com.agenthub.infrastructure.agents.aliyun.session;

import io.agentscope.core.session.InMemorySession;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * AgentScope 会话管理工厂。
 */
@Component
public class SessionFactory {

    public Session createInMemorySession() {
        return new InMemorySession();
    }

    public Session createJsonSession(Path sessionPath) {
        return new JsonSession(sessionPath);
    }
}
