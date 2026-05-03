package com.agenthub.infrastructure.runtime;

import com.agenthub.application.port.out.AgentRuntime;
import com.agenthub.application.port.out.AgentTeamRuntime;
import com.agenthub.application.port.out.RuntimeFactory;
import com.agenthub.infrastructure.runtime.google.GoogleAdkAgentRuntime;
import com.agenthub.infrastructure.runtime.langchain4j.Langchain4jAgentRuntime;
import com.agenthub.infrastructure.runtime.embabel.EmbabelAgentRuntime;
import com.agenthub.infrastructure.runtime.openjiuwen.OpenJiuwenAgentRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuntimeFactoryImpl implements RuntimeFactory {
    
    @Autowired
    private GoogleAdkAgentRuntime googleAdkRuntime;
    
    @Autowired
    private Langchain4jAgentRuntime langchain4jRuntime;
    
    @Autowired
    private EmbabelAgentRuntime embabelRuntime;
    
    @Autowired
    private OpenJiuwenAgentRuntime openjiuwenRuntime;
    
    @Autowired
    private UnifiedAgentTeamRuntime teamRuntime;
    
    @Override
    public AgentRuntime createAgentRuntime(String frameworkType) {
        return switch (frameworkType.toUpperCase()) {
            case "GOOGLE_ADK" -> googleAdkRuntime;
            case "LANGCHAIN4J" -> langchain4jRuntime;
            case "EMBABEL_AGENT" -> embabelRuntime;
            case "OPENJIUWEN" -> openjiuwenRuntime;
            default -> throw new IllegalArgumentException("Unknown: " + frameworkType);
        };
    }
    
    @Override
    public AgentTeamRuntime createAgentTeamRuntime(String frameworkType) {
        return teamRuntime;
    }
    
    @Override
    public boolean isSupported(String frameworkType) {
        return frameworkType.equalsIgnoreCase("GOOGLE_ADK") ||
               frameworkType.equalsIgnoreCase("LANGCHAIN4J") ||
               frameworkType.equalsIgnoreCase("EMBABEL_AGENT") ||
               frameworkType.equalsIgnoreCase("OPENJIUWEN");
    }
    
    @Override
    public String[] getSupportedFrameworks() {
        return new String[]{"GOOGLE_ADK", "LANGCHAIN4J", "EMBABEL_AGENT", "OPENJIUWEN"};
    }
}
