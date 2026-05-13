package com.agenthub.infrastructure.tools.system_tools.base_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResult {
    private boolean success;
    private int exitCode;
    private String output;
    private String error;
    private long executionTime;
}
