package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateAlertCommand;
import com.agenthub.application.dto.AlertOutput;
import com.agenthub.application.port.out.repositories.AlertRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.monitor.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Alert UseCase.
 */
@Component
@RequiredArgsConstructor
public class AlertUseCase {
    private final AlertRepository repository;

    public AlertOutput create(CreateAlertCommand command) {
        Alert alert = Alert.create(command.getAlertLevel(), command.getAlertType(),
                command.getTitle(), command.getMessage());
        Alert saved = repository.save(alert);
        return toOutput(saved);
    }

    public AlertOutput get(String id) {
        return repository.findById(id)
            .map(this::toOutput)
            .orElseThrow(() -> notFound(id));
    }

    public AlertOutput resolve(String id, String resolvedBy) {
        Alert alert = repository.findById(id)
            .orElseThrow(() -> notFound(id));
        alert.resolve(resolvedBy);
        return toOutput(repository.save(alert));
    }

    public List<AlertOutput> listByRun(String runId) {
        return repository.findByRunId(runId).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<AlertOutput> listUnresolved() {
        return repository.findByResolved(false).stream()
            .map(this::toOutput)
            .toList();
    }

    public List<AlertOutput> list() {
        return repository.findAll().stream()
            .map(this::toOutput)
            .toList();
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private AlertOutput toOutput(Alert alert) {
        return new AlertOutput(
            alert.getId(),
            alert.getAlertLevel(),
            alert.getAlertType(),
            alert.getTitle(),
            alert.getMessage(),
            alert.getRunId(),
            alert.getAgentId(),
            alert.getTraceId(),
            alert.getMetadata(),
            alert.isResolved(),
            alert.getResolvedAt(),
            alert.getResolvedBy(),
            alert.getCreatedAt()
        );
    }

    private NotFoundException notFound(String id) {
        return new NotFoundException("Alert not found: " + id);
    }
}
