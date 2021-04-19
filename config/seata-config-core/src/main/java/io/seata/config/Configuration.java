package io.seata.config;

import java.time.Duration;
import java.util.Set;

/**
 * @author: xielongfei
 * @date: 2021/03/24 14:50
 * @description:
 */
public interface Configuration {

    /**
     * Gets short.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the short
     */
    short getShort(String dataId, int defaultValue, long timeoutMills);

    /**
     * Gets short.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the int
     */
    short getShort(String dataId, short defaultValue);

    /**
     * Gets short.
     *
     * @param dataId the data id
     * @return the int
     */
    short getShort(String dataId);

    /**
     * Gets int.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the int
     */
    int getInt(String dataId, int defaultValue, long timeoutMills);

    /**
     * Gets int.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the int
     */
    int getInt(String dataId, int defaultValue);

    /**
     * Gets int.
     *
     * @param dataId the data id
     * @return the int
     */
    int getInt(String dataId);

    /**
     * Gets long.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the long
     */
    long getLong(String dataId, long defaultValue, long timeoutMills);

    /**
     * Gets long.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the long
     */
    long getLong(String dataId, long defaultValue);

    /**
     * Gets long.
     *
     * @param dataId the data id
     * @return the long
     */
    long getLong(String dataId);

    /**
     * Gets duration.
     *
     * @param dataId the data id
     * @return the duration
     */
    Duration getDuration(String dataId);

    /**
     * Gets duration.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the duration
     */
    Duration getDuration(String dataId, Duration defaultValue);

    /**
     * Gets duration.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return he duration
     */
    Duration getDuration(String dataId, Duration defaultValue, long timeoutMills);

    /**
     * Gets boolean.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the boolean
     */
    boolean getBoolean(String dataId, boolean defaultValue, long timeoutMills);

    /**
     * Gets boolean.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the boolean
     */
    boolean getBoolean(String dataId, boolean defaultValue);

    /**
     * Gets boolean.
     *
     * @param dataId the data id
     * @return the boolean
     */
    boolean getBoolean(String dataId);

    /**
     * Gets config.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the config
     */
    String getConfig(String dataId, String defaultValue, long timeoutMills);

    /**
     * Gets config.
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @return the config
     */
    String getConfig(String dataId, String defaultValue);

    /**
     * Gets config.
     *
     * @param dataId       the data id
     * @param timeoutMills the timeout mills
     * @return the config
     */
    String getConfig(String dataId, long timeoutMills);

    /**
     * Gets config.
     *
     * @param dataId the data id
     * @return the config
     */
    String getConfig(String dataId);

    /**
     * Put config boolean.
     *
     * @param dataId       the data id
     * @param content      the content
     * @param timeoutMills the timeout mills
     * @return the boolean
     */
    boolean putConfig(String dataId, String content, long timeoutMills);

    /**
     *
     * @param dataId       the data id
     * @param defaultValue the default value
     * @param timeoutMills the timeout mills
     * @return the Latest config
     */
    String getLatestConfig(String dataId, String defaultValue, long timeoutMills);

    /**
     * Put config boolean.
     *
     * @param dataId  the data id
     * @param content the content
     * @return the boolean
     */
    boolean putConfig(String dataId, String content);

    /**
     * Put config if absent boolean.
     *
     * @param dataId       the data id
     * @param content      the content
     * @param timeoutMills the timeout mills
     * @return the boolean
     */
    boolean putConfigIfAbsent(String dataId, String content, long timeoutMills);

    /**
     * Put config if absent boolean.
     *
     * @param dataId  the data id
     * @param content the content
     * @return the boolean
     */
    boolean putConfigIfAbsent(String dataId, String content);

    /**
     * Remove config boolean.
     *
     * @param dataId       the data id
     * @param timeoutMills the timeout mills
     * @return the boolean
     */
    boolean removeConfig(String dataId, long timeoutMills);

    /**
     * Remove config boolean.
     *
     * @param dataId the data id
     * @return the boolean
     */
    boolean removeConfig(String dataId);

    /**
     * Add config listener.
     *
     * @param dataId   the data id
     * @param listener the listener
     */
    void addConfigListener(String dataId, ConfigurationChangeListener listener);

    /**
     * Remove config listener.
     *
     * @param dataId   the data id
     * @param listener the listener
     */
    void removeConfigListener(String dataId, ConfigurationChangeListener listener);

    /**
     * Gets config listeners.
     *
     * @param dataId the data id
     * @return the config listeners
     */
    Set<ConfigurationChangeListener> getConfigListeners(String dataId);

    /**
     * Gets config from sys pro.
     *
     * @param dataId the data id
     * @return the config from sys pro
     */
    default String getConfigFromSysPro(String dataId) {
        return System.getProperty(dataId);
    }
}
