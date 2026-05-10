package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariableOutput {
    private String name;
    private String description;
    private String defaultValue;
    private boolean required;
}
