package com.agenthub.domain.model;

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
