package com.agenthub.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private /** 用户ID */ String id;
    private /** 用户名 */ String username;
    private /** 租户ID */ String tenantId;
}
