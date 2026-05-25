package com.agenthub.test.unit;

import com.agenthub.application.dto.AlertOutput;
import com.agenthub.application.port.out.repositories.AlertRepository;
import com.agenthub.application.usecase.AlertUseCase;
import com.agenthub.domain.model.monitor.Alert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AlertUseCase 单元测试.
 */
class AlertUseCaseTest {

    @Mock
    private AlertRepository repository;

    private AlertUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new AlertUseCase(repository);
    }

    @Test
    void shouldCreateAlert() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.save(any())).thenReturn(alert);

        AlertOutput result = useCase.create("ERROR", "PERFORMANCE", "Test", "Message");

        assertNotNull(result);
        assertEquals("ERROR", result.getAlertLevel());
        assertEquals("PERFORMANCE", result.getAlertType());
    }

    @Test
    void shouldGetAlertById() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.findById("id-1")).thenReturn(Optional.of(alert));

        AlertOutput result = useCase.get("id-1");

        assertNotNull(result);
        assertEquals("ERROR", result.getAlertLevel());
    }

    @Test
    void shouldThrowExceptionWhenAlertNotFound() {
        when(repository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> useCase.get("non-existent"));
    }

    @Test
    void shouldResolveAlert() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.findById("id-1")).thenReturn(Optional.of(alert));
        when(repository.save(any())).thenReturn(alert);

        AlertOutput result = useCase.resolve("id-1", "user-1");

        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    void shouldListAlertsByRun() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.findByRunId("run-1")).thenReturn(List.of(alert));

        List<AlertOutput> result = useCase.listByRun("run-1");

        assertEquals(1, result.size());
    }

    @Test
    void shouldListUnresolvedAlerts() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.findByResolved(false)).thenReturn(List.of(alert));

        List<AlertOutput> result = useCase.listUnresolved();

        assertEquals(1, result.size());
    }

    @Test
    void shouldListAllAlerts() {
        Alert alert = Alert.create("ERROR", "PERFORMANCE", "Test", "Message");
        when(repository.findAll()).thenReturn(List.of(alert));

        List<AlertOutput> result = useCase.list();

        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteAlert() {
        doNothing().when(repository).deleteById("id-1");

        useCase.delete("id-1");

        verify(repository).deleteById("id-1");
    }
}
