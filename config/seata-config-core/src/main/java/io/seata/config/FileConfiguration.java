package io.seata.config;

import io.netty.util.internal.ConcurrentSet;
import io.seata.common.thread.NamedThreadFactory;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import io.seata.config.file.FileConfig;
import io.seata.config.ConfigFuture.ConfigOperation;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author: xielongfei
 * @date: 2021/03/24 15:23
 * @description:
 */
public class FileConfiguration extends AbstractConfiguration{

    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfiguration.class);

    private FileConfig fileConfig;

    private ExecutorService configOperateExecutor;

    private static final int CORE_CONFIG_OPERATE_THREAD = 1;

    private static final int MAX_CONFIG_OPERATE_THREAD = 2;

    private static final long LISTENER_CONFIG_INTERVAL = 1 * 1000;

    private static final String REGISTRY_TYPE = "file";

    public static final String SYS_FILE_RESOURCE_PREFIX = "file:";

    private final ConcurrentMap<String, Set<ConfigurationChangeListener>> configListenersMap = new ConcurrentHashMap<>(
            8);

    private final Map<String, String> listenedConfigMap = new HashMap<>(8);

    private final String targetFilePath;

    private volatile long targetFileLastModified;

    private final String name;

    private final FileListener fileListener = new FileListener();

    private final boolean allowDynamicRefresh;

    /**
     * Note that:this constructor is only used to create proxy with CGLIB
     * see io.seata.spring.boot.autoconfigure.provider.SpringBootConfigurationProvider#provide
     */
    public FileConfiguration() {
        this.name = null;
        this.targetFilePath = null;
        this.allowDynamicRefresh = false;
    }

    /**
     * Instantiates a new File configuration.
     *
     * @param name the name
     */
    public FileConfiguration(String name) {
        this(name, true);
    }

    public FileConfiguration(String name, boolean allowDynamicRefresh) {
        LOGGER.info("The file name of the operation is {}", name);
        File file = getConfigFile(name);
        if (file == null) {
            targetFilePath = null;
        } else {
            targetFilePath = file.getPath();
            fileConfig = FileConfigFactory.load(file, name);
        }
        /**
         * For seata-server side the conf file should always exists.
         * For application(or client) side,conf file may not exists when using seata-spring-boot-starter
         */
        if (targetFilePath == null) {
            fileConfig = FileConfigFactory.load();
            this.allowDynamicRefresh = false;
        } else {
            targetFileLastModified = new File(targetFilePath).lastModified();
            this.allowDynamicRefresh = allowDynamicRefresh;
        }

        this.name = name;
        configOperateExecutor = new ThreadPoolExecutor(CORE_CONFIG_OPERATE_THREAD, MAX_CONFIG_OPERATE_THREAD,
                Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("configOperate", MAX_CONFIG_OPERATE_THREAD));
    }

    private File getConfigFile(String name) {
        try {
            if (name == null) {
                throw new IllegalArgumentException("name can't be null");
            }
            String filePath = null;
            boolean filePathCustom = name.startsWith(SYS_FILE_RESOURCE_PREFIX);
            if (filePathCustom) {
                filePath = name.substring(SYS_FILE_RESOURCE_PREFIX.length());
            } else {
                filePath = this.getClass().getClassLoader().getResource("").getPath() + name;
            }
            filePath = URLDecoder.decode(filePath, "utf-8");
            File targetFile = new File(filePath);
            if (!targetFile.exists()) {
                for (String s : FileConfigFactory.getSuffixSet()) {
                    targetFile = new File(filePath + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR + s);
                    if (targetFile.exists()) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("The configuration file used is {}", targetFile.getPath());
                        }
                        return targetFile;
                    }
                }
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("The configuration file used is {}", name);
                }
                return targetFile;
            }
            if (!filePathCustom) {
                URL resource = this.getClass().getClassLoader().getResource(name);
                if (resource == null) {
                    for (String s : FileConfigFactory.getSuffixSet()) {
                        resource = this.getClass().getClassLoader().getResource(name + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR + s);
                        if (resource != null) {
                            if (LOGGER.isInfoEnabled()) {
                                LOGGER.info("The configuration file used is {}", resource.getPath());
                            }
                            String path = resource.getPath();
                            path = URLDecoder.decode(path, "utf-8");
                            return new File(path);
                        }
                    }
                } else {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("The configuration file used is {}", name);
                    }
                    String path = resource.getPath();
                    path = URLDecoder.decode(path, "utf-8");
                    return new File(path);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("file not found--" + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        String value = getConfigFromSysPro(dataId);
        if (value != null) {
            return value;
        }
        ConfigFuture configFuture = new ConfigFuture(dataId, defaultValue, ConfigOperation.GET, timeoutMills);
        configOperateExecutor.submit(new ConfigOperateRunnable(configFuture));
        Object getValue = configFuture.get();
        return getValue == null ? null : String.valueOf(getValue);
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        ConfigFuture configFuture = new ConfigFuture(dataId, content, ConfigOperation.PUT, timeoutMills);
        configOperateExecutor.submit(new ConfigOperateRunnable(configFuture));
        return (Boolean) configFuture.get();
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        ConfigFuture configFuture = new ConfigFuture(dataId, content, ConfigOperation.PUTIFABSENT, timeoutMills);
        configOperateExecutor.submit(new ConfigOperateRunnable(configFuture));
        return (Boolean) configFuture.get();
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        ConfigFuture configFuture = new ConfigFuture(dataId, null, ConfigOperation.REMOVE, timeoutMills);
        configOperateExecutor.submit(new ConfigOperateRunnable(configFuture));
        return (Boolean) configFuture.get();
    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        configListenersMap.computeIfAbsent(dataId, key -> new ConcurrentSet<>())
                .add(listener);
        listenedConfigMap.put(dataId, ConfigurationFactory.getInstance().getConfig(dataId));

        // Start config change listener for the dataId.
        fileListener.addListener(dataId, listener);
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        Set<ConfigurationChangeListener> configListeners = getConfigListeners(dataId);
        if (CollectionUtils.isNotEmpty(configListeners)) {
            configListeners.remove(listener);
            if (configListeners.isEmpty()) {
                configListenersMap.remove(dataId);
                listenedConfigMap.remove(dataId);
            }
        }
        listener.onShutDown();
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        return null;
    }

    @Override
    public String getTypeName() {
        return REGISTRY_TYPE;
    }

    class ConfigOperateRunnable implements Runnable {
        private ConfigFuture configFuture;

        /**
         * Instantiates a new Config operate runnable.
         *
         * @param configFuture the config future
         */
        public ConfigOperateRunnable(ConfigFuture configFuture) {
            this.configFuture = configFuture;
        }

        @Override
        public void run() {
            if (configFuture != null) {
                if (configFuture.isTimeout()) {
                    setFailResult(configFuture);
                    return;
                }
                try {
                    if (allowDynamicRefresh) {
                        long tempLastModified = new File(targetFilePath).lastModified();
                        if (tempLastModified > targetFileLastModified) {
                            FileConfig tempConfig = FileConfigFactory.load(new File(targetFilePath), name);
                            if (tempConfig != null) {
                                fileConfig = tempConfig;
                                targetFileLastModified = tempLastModified;
                            }
                        }
                    }
                    if (configFuture.getOperation() == ConfigOperation.GET) {
                        String result = fileConfig.getString(configFuture.getDataId());
                        configFuture.setResult(result);
                    } else if (configFuture.getOperation() == ConfigOperation.PUT) {
                        //todo
                        configFuture.setResult(Boolean.TRUE);
                    } else if (configFuture.getOperation() == ConfigOperation.PUTIFABSENT) {
                        //todo
                        configFuture.setResult(Boolean.TRUE);
                    } else if (configFuture.getOperation() == ConfigOperation.REMOVE) {
                        //todo
                        configFuture.setResult(Boolean.TRUE);
                    }
                } catch (Exception e) {
                    setFailResult(configFuture);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Could not found property {}, try to use default value instead. exception:{}",
                                configFuture.getDataId(), e.getMessage());
                    }
                }
            }
        }

        private void setFailResult(ConfigFuture configFuture) {
            if (configFuture.getOperation() == ConfigOperation.GET) {
                String result = configFuture.getContent();
                configFuture.setResult(result);
            } else {
                configFuture.setResult(Boolean.FALSE);
            }
        }
    }

    /**
     * The type FileListener.
     */
    class FileListener implements ConfigurationChangeListener {
        private final Map<String, Set<ConfigurationChangeListener>> dataIdMap = new HashMap<>();

        private final ExecutorService executor = new ThreadPoolExecutor(CORE_LISTENER_THREAD, MAX_LISTENER_THREAD, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("fileListener", MAX_LISTENER_THREAD));

        /**
         * Instantiates a new FileListener.
         *
         */
        FileListener() {}

        public synchronized void addListener(String dataId, ConfigurationChangeListener listener) {
            // only the first time add listener will trigger on process event
            if (dataIdMap.isEmpty()) {
                fileListener.onProcessEvent(new ConfigurationChangeEvent());
            }

            dataIdMap .computeIfAbsent(dataId, value -> new HashSet<>()).add(listener);
        }

        @Override
        public void onChangeEvent(ConfigurationChangeEvent event) {
            while (true) {
                for (String dataId : dataIdMap.keySet()) {
                    try {
                        String currentConfig =
                                ConfigurationFactory.getInstance().getLatestConfig(dataId, null, DEFAULT_CONFIG_TIMEOUT);
                        if (StringUtils.isNotBlank(currentConfig)) {
                            String oldConfig = listenedConfigMap.get(dataId);
                            if (ObjectUtils.notEqual(currentConfig, oldConfig)) {
                                listenedConfigMap.put(dataId, currentConfig);
                                event.setDataId(dataId).setNewValue(currentConfig).setOldValue(oldConfig);

                                for (ConfigurationChangeListener listener : dataIdMap.get(dataId)) {
                                    listener.onChangeEvent(event);
                                }
                            }
                        }
                    } catch (Exception exx) {
                        LOGGER.error("fileListener execute error, dataId :{}", dataId, exx);
                    }
                }
                try {
                    Thread.sleep(LISTENER_CONFIG_INTERVAL);
                } catch (InterruptedException e) {
                    LOGGER.error("fileListener thread sleep error:{}", e.getMessage());
                }
            }
        }

        @Override
        public ExecutorService getExecutorService() {
            return executor;
        }
    }
}
