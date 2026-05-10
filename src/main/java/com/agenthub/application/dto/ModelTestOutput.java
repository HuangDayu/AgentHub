package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelTestOutput {
    private boolean success;
    private String message;
    private String details;
}
