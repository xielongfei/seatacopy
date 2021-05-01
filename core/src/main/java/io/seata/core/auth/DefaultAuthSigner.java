package io.seata.core.auth;

import io.seata.common.loader.LoadLevel;

/**
 * @author: xielongfei
 * @date: 2021/04/30 10:58
 * @description:
 */
@LoadLevel(name = "defaultAuthSigner", order = 100)
public class DefaultAuthSigner implements AuthSigner{
    @Override
    public String sign(String data, String key) {
        return data;
    }
}
