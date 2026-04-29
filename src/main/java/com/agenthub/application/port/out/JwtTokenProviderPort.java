package com.agenthub.application.port.out;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.util.List;

/**
 * JWT令牌提供者端口接口。
 * 定义application层与infrastructure层的边界。
 */
public interface JwtTokenProviderPort {
    
    /**
     * 生成访问令牌，包含用户ID、租户ID和角色信息。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param roles    用户角色列表
     * @return JWT令牌字符串
     */
    String generateAccessToken(String userId, String tenantId, List<String> roles);
    
    /**
     * 生成刷新令牌。
     *
     * @param userId 用户ID
     * @return JWT刷新令牌字符串
     */
    String generateRefreshToken(String userId);
    
    /**
     * 验证并解析JWT令牌，返回令牌声明。
     *
     * @param token JWT令牌字符串
     * @return 令牌声明对象
     * @throws JwtException 令牌无效或已过期时抛出
     */
    Claims validateToken(String token) throws JwtException;
    
    /**
     * 获取访问令牌的过期时间（秒）。
     *
     * @return 过期秒数
     */
    long getAccessTokenExpirySeconds();
}
