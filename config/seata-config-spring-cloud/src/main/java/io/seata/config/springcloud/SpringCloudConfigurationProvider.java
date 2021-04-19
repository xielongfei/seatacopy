package io.seata.config.springcloud;

import io.seata.common.loader.LoadLevel;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationProvider;

/**
 * @author: xielongfei
 * @date: 2021/03/29 10:59
 * @description:
 */
@LoadLevel(name = "SpringCloudConfig", order = 1)
public class SpringCloudConfigurationProvider implements ConfigurationProvider {

    @Override
    public Configuration provide() {
        return SpringCloudConfiguration.getInstance();
    }
}
