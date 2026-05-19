package com.agenthub.application.factory;

import com.agenthub.domain.model.AbstractReActAgent;
import com.agenthub.domain.model.ReActAgentContext;

/**
 * @author huangdayu
 */
public interface ReActAgentFactory {

    AbstractReActAgent create(ReActAgentContext reActAgentContext);

}
