package com.agenthub.application.port.out;

/**
 * 凭据验证器端口.
 * <p>
 * 定义验证用户凭据的领域接口。
 * </p>
 */
public interface CredentialVerifierPort {

    /**
     * 验证用户名和密码是否匹配。
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回true，否则返回false
     */
    boolean verify(String username, String password);
}
