package com.agenthub.application.port.out;

import com.agenthub.domain.model.auth.AccessToken;

/**
 * 访问令牌服务端口.
 * <p>
 * 定义签发访问令牌的领域接口。
 * </p>
 */
public interface AccessTokenPort {

    /**
     * 为指定用户主体签发访问令牌。
     *
     * @param subject 用户主体标识（通常为用户ID）
     * @return 签发的访问令牌
     */
    AccessToken issueToken(String subject);
}
