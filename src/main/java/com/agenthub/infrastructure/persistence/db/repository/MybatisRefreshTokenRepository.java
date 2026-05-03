package com.agenthub.infrastructure.persistence.db.repository;

import com.agenthub.domain.model.RefreshTokenSession;
import com.agenthub.application.port.out.repositories.RefreshTokenRepository;
import com.agenthub.infrastructure.persistence.db.entity.RefreshTokenSessionEntity;
import com.agenthub.infrastructure.persistence.db.mapper.RefreshTokenSessionMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 基于MyBatis的刷新令牌仓储实现.
 * <p>
 * 通过数据库持久化刷新令牌会话。
 * </p>
 */
@Component
@Primary
public class MybatisRefreshTokenRepository implements RefreshTokenRepository {
    private final RefreshTokenSessionMapper mapper;

    /**
     * 构造基于MyBatis的刷新令牌仓储。
     *
     * @param mapper 刷新令牌会话数据映射器
     */
    public MybatisRefreshTokenRepository(RefreshTokenSessionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存刷新令牌会话，如果不存在则插入，否则更新。
     *
     * @param session 待保存的刷新令牌会话
     */
    @Override
    public void save(RefreshTokenSession session) {
        RefreshTokenSessionEntity po = toPo(session);
        // 检查会话是否已存在，决定插入或更新
        if (mapper.selectById(po.getToken()) == null) {
            mapper.insert(po);
            return;
        }
        mapper.updateById(po);
    }

    /**
     * 根据令牌值查找刷新令牌会话。
     *
     * @param token 刷新令牌值
     * @return 包含会话的Optional，不存在时为空
     */
    @Override
    public Optional<RefreshTokenSession> findByToken(String token) {
        return Optional.ofNullable(mapper.selectById(token)).map(this::toDomain);
    }

    /**
     * 根据令牌值删除刷新令牌会话。
     *
     * @param token 刷新令牌值
     */
    @Override
    public void deleteByToken(String token) {
        mapper.deleteById(token);
    }

    /**
     * 将领域模型转换为持久化对象。
     *
     * @param session 刷新令牌会话领域模型
     * @return 持久化对象
     */
    private RefreshTokenSessionEntity toPo(RefreshTokenSession session) {
        RefreshTokenSessionEntity po = new RefreshTokenSessionEntity();
        po.setToken(session.token());
        po.setSubject(session.subject());
        po.setExpiresAt(session.expiresAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域模型。
     *
     * @param po 持久化对象
     * @return 刷新令牌会话领域模型
     */
    private RefreshTokenSession toDomain(RefreshTokenSessionEntity po) {
        return new RefreshTokenSession(po.getToken(), po.getSubject(), po.getExpiresAt());
    }
}
