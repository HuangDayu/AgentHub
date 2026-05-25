package com.agenthub.infrastructure.telemetry;

import io.agentscope.core.studio.pojo.PushMessageRequest;
import io.agentscope.core.studio.pojo.RegisterRunRequest;
import io.agentscope.core.studio.pojo.RequestUserInputRequest;

/**
 * @author huangdayu
 */
public interface AgentStudioMessageHandler {
    void registerRun(RegisterRunRequest payload);

    void pushMessage(PushMessageRequest payload);

    void requestUserInput(RequestUserInputRequest payload);
}
