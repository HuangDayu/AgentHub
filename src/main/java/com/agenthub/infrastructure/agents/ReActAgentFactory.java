package com.agenthub.infrastructure.agents;

/**
 * @author huangdayu
 */
public interface ReActAgentFactory {

    AbstractReActAgent create(ReActAgentContext reActAgentContext);

}
