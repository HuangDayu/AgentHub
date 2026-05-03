package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 刷新令牌会话持久化对象.
 * <p>
 * 映射iam_refresh_token_session表记录。
 * </p>
 */
@TableName("app.iam_refresh_token_session")
public class RefreshTokenSessionEntity {
    /** 刷新令牌（主键） */
    @TableId
    private String token;
    /** 用户主体（通常是用户ID） */
    private String subject;
    /** 过期时间 */
    private Instant expiresAt;

    /**
     * 获取刷新令牌。
     *
     * @return 刷新令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置刷新令牌。
     *
     * @param token 刷新令牌
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取用户主体。
     *
     * @return 用户主体
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 设置用户主体。
     *
     * @param subject 用户主体
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 获取过期时间。
     *
     * @return 过期时间
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间。
     *
     * @param expiresAt 过期时间
     */
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
