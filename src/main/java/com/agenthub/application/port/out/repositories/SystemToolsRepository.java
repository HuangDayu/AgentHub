package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.tools.SystemTool;

import java.util.List;
import java.util.Optional;

public interface SystemToolsRepository {

    SystemTool insertOrUpdate(SystemTool tool);

    List<SystemTool> syncTools(List<SystemTool> tools);

    Optional<SystemTool> findById(String id);

    List<SystemTool> findAll();

    List<SystemTool> findByEnabled(boolean enabled);

    Optional<SystemTool> findByToolClassName(String toolClassName);

    void deleteById(String id);

    void updateEnabled(String id, boolean enabled);

    List<SystemTool> findByWorkspaceId(String workspaceId);

    List<SystemTool> findByIds(List<String> toolIds);


}
