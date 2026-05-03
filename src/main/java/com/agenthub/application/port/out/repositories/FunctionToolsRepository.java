package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.FunctionTool;

import java.util.List;
import java.util.Optional;

public interface FunctionToolsRepository {

    FunctionTool save(FunctionTool tool);

    Optional<FunctionTool> findById(String id);

    List<FunctionTool> findAll();

    List<FunctionTool> findByEnabled(boolean enabled);

    Optional<FunctionTool> findByToolClassName(String toolClassName);

    void deleteById(String id);

    void updateEnabled(String id, boolean enabled);
}
