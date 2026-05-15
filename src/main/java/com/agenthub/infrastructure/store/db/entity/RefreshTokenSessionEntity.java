package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * 刷新令牌会话持久化对象.
 * <p>
 * 映射iam_refresh_token_session表记录。
 * </p>
 */
@Data
@TableName("iam_refresh_token_session")
public class RefreshTokenSessionEntity {
    /** 刷新令牌（主键） */
    @TableId
    private String token;
    /** 用户主体（通常是用户ID） */
    private String subject;
    /** 过期时间 */
    private Instant expiresAt;


}
