package com.agenthub.domain.model.agent;

import com.agenthub.domain.enums.AgentToolType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AgentToolInfo {

    private AgentToolType type;
    private String configId;
    private String name;
    private String description;
    private boolean enabled;

    public AgentToolInfo(AgentToolType type) {
        this.type = type;
    }

}
