package com.agenthub.application.port.out.tools;

import com.agenthub.domain.model.tools.SystemTool;

import java.util.List;

public interface SystemToolScannerPort {
    List<SystemTool> scanSystemTools();
}
