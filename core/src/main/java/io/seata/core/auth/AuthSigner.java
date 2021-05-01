package io.seata.core.auth;

/**
 * @author: xielongfei
 * @date: 2021/04/30 10:57
 * @description:
 */
public interface AuthSigner {
    String sign(String data, String key);
}
