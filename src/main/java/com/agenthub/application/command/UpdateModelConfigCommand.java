package com.agenthub.application.command;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.enums.ModelType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModelConfigCommand {
    private String id;
    private String name;
    private ModelType type;
    private ModelSupplier supplier;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean enabled;
}
