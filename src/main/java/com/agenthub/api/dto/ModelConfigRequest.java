package com.agenthub.api.dto;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.enums.ModelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigRequest {
    private @NotBlank String name;
    private @NotNull ModelType type;
    private @NotNull ModelSupplier supplier;
    private @NotBlank String apiKey;
    private String baseUrl;
    private String model;
    private Boolean enabled;
}
