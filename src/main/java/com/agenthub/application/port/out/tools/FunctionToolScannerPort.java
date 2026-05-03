package com.agenthub.application.port.out.tools;

import com.agenthub.domain.model.FunctionTool;

import java.util.List;

public interface FunctionToolScannerPort {
    List<FunctionTool> scanFunctionTools();
}
