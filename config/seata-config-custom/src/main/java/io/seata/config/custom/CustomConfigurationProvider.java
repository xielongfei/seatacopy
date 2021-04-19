package io.seata.config.custom;

import io.seata.common.loader.EnhancedServiceLoader;
import io.seata.common.loader.LoadLevel;
import io.seata.common.util.StringUtils;
import io.seata.config.*;

import java.util.stream.Stream;

/**
 * @author: xielongfei
 * @date: 2021/03/29 14:38
 * @description:
 */
@LoadLevel(name = "Custom")
public class CustomConfigurationProvider implements ConfigurationProvider {
    @Override
    public Configuration provide() {
        String pathDataId = ConfigurationKeys.FILE_ROOT_CONFIG + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR
                + ConfigType.Custom.name().toLowerCase() + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR
                + "name";
        String name = ConfigurationFactory.CURRENT_FILE_INSTANCE.getConfig(pathDataId);
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name value of custom config type must not be blank");
        }
        if (Stream.of(ConfigType.values()).anyMatch(ct -> ct.name().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException(String.format("custom config type name %s is not allowed", name));
        }
        return EnhancedServiceLoader.load(ConfigurationProvider.class, name).provide();
    }
}
