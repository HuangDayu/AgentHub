package com.agenthub.application.command;

import lombok.Data;
import java.util.Map;

@Data
public class AddNodeCommand {
    private String type;
    private String name;
    private Map<String, Object> position;
    private Map<String, Object> config;
}
