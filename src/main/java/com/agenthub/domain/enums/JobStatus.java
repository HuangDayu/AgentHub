package com.agenthub.domain.enums;

/**
 * Ingestion job status values.
 * Domain states: PENDING → PARSING → CLEANING → CHUNKING → VECTORIZING → COMPLETED/FAILED
 */
public enum JobStatus {
    PENDING,
    PARSING,
    CLEANING,
    CHUNKING,
    VECTORIZING,
    COMPLETED,
    FAILED
}
