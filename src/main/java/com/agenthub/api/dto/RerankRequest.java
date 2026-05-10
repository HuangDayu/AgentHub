package com.agenthub.api.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RerankRequest {
    private String query;
    private List<String> candidates;
}
