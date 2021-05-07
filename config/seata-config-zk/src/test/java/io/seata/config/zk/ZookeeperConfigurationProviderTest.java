package io.seata.config.zk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author: xielongfei
 * @date: 2021/05/01 22:47
 * @description:
 */
public class ZookeeperConfigurationProviderTest {

    @Test
    public void provide() {
        ZookeeperConfigurationProvider provider = new ZookeeperConfigurationProvider();
        Assertions.assertTrue(provider.provide() instanceof ZookeeperConfiguration);
    }
}
