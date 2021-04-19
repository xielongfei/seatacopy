package io.seata.config.zk;

import io.seata.common.loader.LoadLevel;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationProvider;

/**
 * @author: xielongfei
 * @date: 2021/03/28 18:11
 * @description:
 */
@LoadLevel(name = "ZK", order = 1)
public class ZookeeperConfigurationProvider implements ConfigurationProvider {
    @Override
    public Configuration provide() {
        try {
            return new ZookeeperConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
