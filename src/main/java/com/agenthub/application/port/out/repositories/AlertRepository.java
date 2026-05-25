package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.monitor.Alert;

import java.util.List;
import java.util.Optional;

/**
 * Alert Repository 接口.
 */
public interface AlertRepository {
    Alert save(Alert alert);

    Optional<Alert> findById(String id);

    List<Alert> findByRunId(String runId);

    List<Alert> findByResolved(boolean resolved);

    List<Alert> findAll();

    void deleteById(String id);
}
