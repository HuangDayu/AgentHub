package com.agenthub.application.port.out;

/**
 * 刷新令牌生成器端口.
 * <p>
 * 定义生成刷新令牌的领域接口。
 * </p>
 */
public interface RefreshTokenGenerator {

    /**
     * 生成一个新的刷新令牌字符串。
     *
     * @return 生成的刷新令牌
     */
    String generate();
}
