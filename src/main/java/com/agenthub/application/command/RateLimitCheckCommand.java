package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 速率限制检查命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitCheckCommand {
    private String userId;
    private String dataSourceId;
    private int perMinute;
    private int perHour;
}
