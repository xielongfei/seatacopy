package io.seata.config.springcloud;

import io.seata.common.holder.ObjectHolder;
import io.seata.common.util.StringUtils;
import io.seata.config.AbstractConfiguration;
import io.seata.config.ConfigurationChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;

/**
 * @author: xielongfei
 * @date: 2021/03/29 11:01
 * @description:
 */
public class SpringCloudConfiguration extends AbstractConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudConfiguration.class);
    private static final String CONFIG_TYPE = "SpringCloudConfig";
    private static volatile SpringCloudConfiguration instance;
    private static final String PREFIX = "seata.";

    public static SpringCloudConfiguration getInstance() {
        if (instance == null) {
            synchronized (SpringCloudConfiguration.class) {
                if (instance == null) {
                    instance = new SpringCloudConfiguration();
                }
            }
        }
        return instance;
    }

    private SpringCloudConfiguration() {

    }

    @Override
    public String getTypeName() {
        return CONFIG_TYPE;
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        ApplicationContext applicationContext = ObjectHolder.INSTANCE.getObject(ApplicationContext.class);
        if (applicationContext == null || applicationContext.getEnvironment() == null) {
            return defaultValue;
        }
        String conf = applicationContext.getEnvironment().getProperty(PREFIX + dataId);
        return StringUtils.isNotBlank(conf) ? conf : defaultValue;
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        return false;
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        return false;
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        return false;
    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        LOGGER.warn("dynamic listening is not supported spring cloud config");
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        return null;
    }
}
