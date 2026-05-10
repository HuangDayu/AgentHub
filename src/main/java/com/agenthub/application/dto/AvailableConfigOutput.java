package com.agenthub.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableConfigOutput {
    private String id;
    private String name;
    private String description;
}
