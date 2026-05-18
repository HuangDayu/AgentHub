package com.agenthub.application.command;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.enums.ModelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelConfigCommand {
    private String name;
    private ModelType type;
    private ModelSupplier supplier;
    private String apiKey;
    private String baseUrl;
    private String model;
    private Boolean enabled;
    private String createdBy;
}
