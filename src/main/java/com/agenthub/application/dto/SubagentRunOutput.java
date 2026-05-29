package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子Agent运行输出。
 */
@Data
@NoArgsConstructor
public class SubagentRunOutput {
    private String subagentId;
    private String subsessionId;
    private String status;
    private String result;
}
