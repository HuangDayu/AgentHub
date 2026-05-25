package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.studio.UserInputPrompt;

import java.util.List;
import java.util.Optional;

/**
 * 用户输入请求仓储接口.
 */
public interface UserInputRequestRepository {
    UserInputPrompt save(UserInputPrompt request);
    Optional<UserInputPrompt> findByRequestId(String requestId);
    List<UserInputPrompt> findByRunId(String runId);
    List<UserInputPrompt> findAll();
    void deleteById(String id);
}
