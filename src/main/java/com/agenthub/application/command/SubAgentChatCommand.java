package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubAgentChatCommand {

    private String subAgentId;
    private String subSessionId;
    private String userMessage;
    private List<String> filePaths;

}
