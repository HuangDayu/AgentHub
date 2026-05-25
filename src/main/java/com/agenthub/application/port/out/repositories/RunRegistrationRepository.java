package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.studio.RunRegistration;

import java.util.List;
import java.util.Optional;

/**
 * Run注册仓储接口.
 */
public interface RunRegistrationRepository {
    RunRegistration save(RunRegistration registration);
    Optional<RunRegistration> findById(String id);
    List<RunRegistration> findByProject(String project);
    List<RunRegistration> findAll();
    void deleteById(String id);
}
