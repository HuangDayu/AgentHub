package com.agenthub.domain.model;

import java.time.Instant;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeSession {
    private String id;
    private String title;
    private Instant createdAt;
}
