package io.seata.discovery.registry.consul;

import io.seata.common.loader.LoadLevel;
import io.seata.discovery.registry.RegistryProvider;
import io.seata.discovery.registry.RegistryService;

/**
 * @author: xielongfei
 * @date: 2021/04/20 11:16
 * @description:
 */
@LoadLevel(name = "Consul", order = 1)
public class ConsulRegistryProvider implements RegistryProvider {

    @Override
    public RegistryService provide() {
        return ConsulRegistryServiceImpl.getInstance();
    }
}
