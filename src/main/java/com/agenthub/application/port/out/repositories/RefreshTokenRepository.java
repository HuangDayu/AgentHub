package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.auth.RefreshTokenSession;

import java.util.Optional;

/**
 * 刷新令牌仓储端口.
 * <p>
 * 定义刷新令牌会话持久化的领域接口。
 * </p>
 */
public interface RefreshTokenRepository {

    /**
     * 保存刷新令牌会话。
     *
     * @param session 待保存的刷新令牌会话
     */
    void save(RefreshTokenSession session);

    /**
     * 根据令牌值查找刷新令牌会话。
     *
     * @param token 刷新令牌值
     * @return 包含会话的Optional，不存在时为空
     */
    Optional<RefreshTokenSession> findByToken(String token);

    /**
     * 根据令牌值删除刷新令牌会话。
     *
     * @param token 刷新令牌值
     */
    void deleteByToken(String token);
}
