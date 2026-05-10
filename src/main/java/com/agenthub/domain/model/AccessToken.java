package com.agenthub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    private /** JWT令牌字符串 */ String tokenValue;
    private /** 过期时间（秒） */ long expiresInSeconds;
}
