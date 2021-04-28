package io.seata.discovery.registry.consul;

import com.ecwid.consul.v1.health.model.HealthService;

import java.util.List;

/**
 * @author: xielongfei
 * @date: 2021/04/19 18:21
 * @description:
 */
public interface ConsulListener {
    /**
     * on event
     *
     * @param services
     */
    void onEvent(List<HealthService> services);
}
