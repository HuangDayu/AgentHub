package com.agenthub.application.factory;

import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface ReActAgentFactory {

    AbstractReActAgent create(ReActAgentContext reActAgentContext);

}
