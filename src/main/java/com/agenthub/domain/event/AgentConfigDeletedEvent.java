package com.agenthub.domain.event;

import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.AgentConfigCategory;
import com.agenthub.domain.model.AgentConfigType;
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
public class AgentConfigDeletedEvent {

    private List<AgentConfig> configs;
    private AgentConfigCategory category;
    private AgentConfigType type;

}
