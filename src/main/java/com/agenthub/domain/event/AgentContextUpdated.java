package com.agenthub.domain.event;

import lombok.Data;

/**
 * @author huangdayu
 */
@Data
public class AgentContextUpdated {

    private String agentId;
    private String configType;

}
