package io.seata.config;

/**
 * @author: xielongfei
 * @date: 2021/03/24 13:58
 * @description:
 */
public enum ConfigType {

    /**
     * File config type.
     */
    File,
    /**
     * zookeeper config type.
     */
    ZK,
    /**
     * Nacos config type.
     */
    Nacos,
    /**
     * Apollo config type.
     */
    Apollo,
    /**
     * Consul config type
     */
    Consul,
    /**
     * Etcd3 config type
     */
    Etcd3,
    /**
     * spring cloud config type
     */
    SpringCloudConfig,
    /**
     * Custom config type
     */
    Custom;

    /**
     * Gets type.
     *
     * @param name the name
     * @return the type
     */
    public static ConfigType getType(String name) {
        for (ConfigType configType : values()) {
            if (configType.name().equalsIgnoreCase(name)) {
                return configType;
            }
        }
        throw new IllegalArgumentException("not support config type: " + name);
    }
}
