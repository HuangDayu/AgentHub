package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertCommand {
    private String alertLevel;
    private String alertType;
    private String title;
    private String message;
}
