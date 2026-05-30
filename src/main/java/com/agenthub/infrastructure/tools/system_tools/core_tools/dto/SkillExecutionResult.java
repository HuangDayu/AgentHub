package com.agenthub.infrastructure.tools.system_tools.core_tools.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技能执行结果 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillExecutionResult {
    private boolean success;
    private String message;
    private List<String> stepOutputs;
}
