package io.seata.config;

import io.seata.common.exception.NotSupportYetException;
import io.seata.common.loader.EnhancedServiceLoader;
import io.seata.common.loader.EnhancedServiceNotFoundException;
import io.seata.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author: xielongfei
 * @date: 2021/03/25 11:10
 * @description:
 */
public final class ConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFactory.class);

    private static final String REGISTRY_CONF_DEFAULT = "registry";
    private static final String ENV_SYSTEM_KEY = "SEATA_ENV";
    public static final String ENV_PROPERTY_KEY = "seataEnv";

    private static final String SYSTEM_PROPERTY_SEATA_CONFIG_NAME = "seata.config.name";

    private static final String ENV_SEATA_CONFIG_NAME = "SEATA_CONFIG_NAME";

    public static Configuration CURRENT_FILE_INSTANCE;

    static {
        load();
    }

    private static void load() {
        String seataConfigName = System.getProperty(SYSTEM_PROPERTY_SEATA_CONFIG_NAME);
        if (seataConfigName == null) {
            seataConfigName = System.getenv(ENV_SEATA_CONFIG_NAME);
        }
        if (seataConfigName == null) {
            seataConfigName = REGISTRY_CONF_DEFAULT;
        }
        String envValue = System.getProperty(ENV_PROPERTY_KEY);
        if (envValue == null) {
            envValue = System.getenv(ENV_SYSTEM_KEY);
        }
        Configuration configuration = (envValue == null) ? new FileConfiguration(seataConfigName,
                false) : new FileConfiguration(seataConfigName + "-" + envValue, false);
        Configuration extConfiguration = null;
        try {
            extConfiguration = EnhancedServiceLoader.load(ExtConfigurationProvider.class).provide(configuration);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("load Configuration:{}", extConfiguration == null ? configuration.getClass().getSimpleName()
                        : extConfiguration.getClass().getSimpleName());
            }
        } catch (EnhancedServiceNotFoundException ignore) {

        } catch (Exception e) {
            LOGGER.error("failed to load extConfiguration:{}", e.getMessage(), e);
        }
        CURRENT_FILE_INSTANCE = extConfiguration == null ? configuration : extConfiguration;
    }

    private static final String NAME_KEY = "name";
    private static final String FILE_TYPE = "file";

    private static volatile Configuration instance = null;

    public static Configuration getInstance() {
        if (instance == null) {
            synchronized (Configuration.class) {
                if (instance == null) {
                    instance = buildConfiguration();
                }
            }
        }
        return instance;
    }

    private static Configuration buildConfiguration() {
        ConfigType configType;
        String configTypeName;
        try {
            configTypeName = CURRENT_FILE_INSTANCE.getConfig(
                    ConfigurationKeys.FILE_ROOT_CONFIG + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR
                            + ConfigurationKeys.FILE_ROOT_TYPE);
            if (StringUtils.isBlank(configTypeName)) {
                throw new NotSupportYetException("config type can not be null");
            }
            configType = ConfigType.getType(configTypeName);
        } catch (Exception e) {
            throw e;
        }
        Configuration extConfiguration = null;
        Configuration configuration;
        if (ConfigType.File == configType) {
            String pathDataId = String.join(ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR,
                    ConfigurationKeys.FILE_ROOT_CONFIG, FILE_TYPE, NAME_KEY);
            String name = CURRENT_FILE_INSTANCE.getConfig(pathDataId);
            configuration = new FileConfiguration(name);
            try {
                extConfiguration = EnhancedServiceLoader.load(ExtConfigurationProvider.class).provide(configuration);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("load Configuration:{}", extConfiguration == null
                            ? configuration.getClass().getSimpleName() : extConfiguration.getClass().getSimpleName());
                }
            } catch (EnhancedServiceNotFoundException ignore) {

            } catch (Exception e) {
                LOGGER.error("failed to load extConfiguration:{}", e.getMessage(), e);
            }
        } else {
            configuration = EnhancedServiceLoader.load(ConfigurationProvider.class, Objects.requireNonNull(configType).name()).provide();
        }
        try {
            Configuration configurationCache;
            if (null != extConfiguration) {
                configurationCache = ConfigurationCache.getInstance().proxy(extConfiguration);
            } else {
                configurationCache = ConfigurationCache.getInstance().proxy(configuration);
            }
            if (null != configurationCache) {
                extConfiguration = configurationCache;
            }
        } catch (EnhancedServiceNotFoundException ignore) {

        } catch (Exception e) {
            LOGGER.error("failed to load configurationCacheProvider:{}", e.getMessage(), e);
        }
        return null == extConfiguration ? configuration : extConfiguration;
    }

    protected static void reload() {
        ConfigurationCache.getInstance().clear();
        load();
        instance = null;
        getInstance();
    }
}
