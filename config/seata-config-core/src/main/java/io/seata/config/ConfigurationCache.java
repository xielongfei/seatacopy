package io.seata.config;

import io.seata.common.util.StringUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xielongfei
 * @date: 2021/03/25 14:30
 * @description:
 */
public class ConfigurationCache implements ConfigurationChangeListener {

    private static final String METHOD_PREFIX = "get";

    private static final String METHOD_LATEST_CONFIG = METHOD_PREFIX + "LatestConfig";

    private static final ConcurrentHashMap<String, Object> CONFIG_CACHE = new ConcurrentHashMap<>();

    private Map<String, HashSet<ConfigurationChangeListener>> configListenersMap = new HashMap<>();

    public static void addConfigListener(String dataId, ConfigurationChangeListener... listeners) {
        if (StringUtils.isBlank(dataId)) {
            return;
        }
        synchronized (ConfigurationCache.class) {
            HashSet<ConfigurationChangeListener> listenerHashSet =
                    getInstance().configListenersMap.computeIfAbsent(dataId, key -> new HashSet<>());
            if (!listenerHashSet.contains(getInstance())) {
                ConfigurationFactory.getInstance().addConfigListener(dataId, getInstance());
                listenerHashSet.add(getInstance());
            }
            if (null != listeners && listeners.length > 0) {
                for (ConfigurationChangeListener listener : listeners) {
                    if (!listenerHashSet.contains(listener)) {
                        listenerHashSet.add(listener);
                        ConfigurationFactory.getInstance().addConfigListener(dataId, listener);
                    }
                }
            }
        }
    }

    public static ConfigurationCache getInstance() {
        return ConfigurationCacheInstance.INSTANCE;
    }

    @Override
    public void onChangeEvent(ConfigurationChangeEvent event) {
        Object oldValue = CONFIG_CACHE.get(event.getDataId());
        if (null == oldValue || !oldValue.equals(event.getNewValue())) {
            if (StringUtils.isNotBlank(event.getNewValue())) {
                CONFIG_CACHE.put(event.getDataId(), event.getNewValue());
            } else {
                CONFIG_CACHE.remove(event.getDataId());
            }
        }
    }

    /**
     * cglib动态代理针对类代理，通过create()方法得到代理对象
     * 对这个对象所有非final方法的调用会被转发到该类的intercept()方法
     * @param originalConfiguration
     * @return
     */
    public Configuration proxy(Configuration originalConfiguration) {
        return (Configuration)Enhancer.create(Configuration.class,
                (MethodInterceptor)(proxy, method, args, methodProxy) -> {
                    if (method.getName().startsWith(METHOD_PREFIX)
                            && !method.getName().equalsIgnoreCase(METHOD_LATEST_CONFIG)) {
                        String rawDataId = (String) args[0];
                        Object result = CONFIG_CACHE.get(rawDataId);
                        if (null == result) {
                            result = method.invoke(originalConfiguration, args);
                            if (result != null) {
                                CONFIG_CACHE.put(rawDataId, result);
                            }
                        }
                        if (null != result && method.getReturnType().equals(String.class)) {
                            return String.valueOf(result);
                        }
                        return result;
                    }
                    return method.invoke(originalConfiguration, args);
                });
    }

    private static class ConfigurationCacheInstance {
        private static final ConfigurationCache INSTANCE = new ConfigurationCache();
    }

    public void clear() {
        CONFIG_CACHE.clear();
    }
}
