package com.agenthub.domain.event;

import com.agenthub.domain.model.agent.AgentConfig;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangdayu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfigUpdatedEvent {

    private List<AgentConfig> configs;
    private AgentConfigCategory category;
    private AgentConfigType type;

}
