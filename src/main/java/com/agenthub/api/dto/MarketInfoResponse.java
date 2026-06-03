package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市场信息响应。
 */
@Data
@NoArgsConstructor
public class MarketInfoResponse {
    private String marketId;
    private String marketName;
}
