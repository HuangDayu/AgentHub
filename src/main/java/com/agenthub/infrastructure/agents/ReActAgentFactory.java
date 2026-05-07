package com.agenthub.infrastructure.agents;

import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface ReActAgentFactory {

    AbstractReActAgent create(ReActAgentContext reActAgentContext);

}
