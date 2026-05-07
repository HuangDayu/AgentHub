package com.agenthub.application.port.out.tools;

import com.agenthub.domain.model.SystemTool;

import java.util.List;

public interface SystemToolScannerPort {
    List<SystemTool> scanSystemTools();
}
