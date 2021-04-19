package io.seata.config.zk;

import io.seata.common.exception.NotSupportYetException;
import io.seata.common.thread.NamedThreadFactory;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import io.seata.config.*;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static io.seata.config.ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR;
import static io.seata.config.ConfigurationKeys.FILE_ROOT_CONFIG;
import static io.seata.config.ConfigurationKeys.SEATA_FILE_ROOT_CONFIG;

/**
 * @author: xielongfei
 * @date: 2021/03/28 18:13
 * @description:
 */
public class ZookeeperConfiguration extends AbstractConfiguration {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZookeeperConfiguration.class);

    private static final String CONFIG_TYPE = "zk";
    private static final String ZK_PATH_SPLIT_CHAR = "/";
    private static final String ROOT_PATH = ZK_PATH_SPLIT_CHAR + SEATA_FILE_ROOT_CONFIG;
    private static final Configuration FILE_CONFIG = ConfigurationFactory.CURRENT_FILE_INSTANCE;
    private static final String SERVER_ADDR_KEY = "serverAddr";
    private static final String SESSION_TIMEOUT_KEY = "sessionTimeout";
    private static final String CONNECT_TIMEOUT_KEY = "connectTimeout";
    private static final String AUTH_USERNAME = "username";
    private static final String AUTH_PASSWORD = "password";
    private static final String SERIALIZER_KEY = "serializer";
    private static final int THREAD_POOL_NUM = 1;
    private static final int DEFAULT_SESSION_TIMEOUT = 6000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 2000;
    private static final String FILE_CONFIG_KEY_PREFIX = FILE_ROOT_CONFIG + FILE_CONFIG_SPLIT_CHAR + CONFIG_TYPE
            + FILE_CONFIG_SPLIT_CHAR;
    private static final ExecutorService CONFIG_EXECUTOR = new ThreadPoolExecutor(THREAD_POOL_NUM, THREAD_POOL_NUM,
            Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("ZKConfigThread", THREAD_POOL_NUM));
    private static volatile ZkClient zkClient;
    private static final int MAP_INITIAL_CAPACITY = 8;
    private ConcurrentMap<String, ConcurrentMap<ConfigurationChangeListener, ZKListener>> configListenersMap
            = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);

    public ZookeeperConfiguration() {
        if (zkClient == null) {
            synchronized (ZookeeperConfiguration.class) {
                if (zkClient == null) {
                    ZkSerializer zkSerializer = getZkSerializer();
                    String serverAddr = FILE_CONFIG.getConfig(FILE_CONFIG_KEY_PREFIX + SERVER_ADDR_KEY);
                    int sessionTimeout = FILE_CONFIG.getInt(FILE_CONFIG_KEY_PREFIX + SESSION_TIMEOUT_KEY, DEFAULT_SESSION_TIMEOUT);
                    int connectTimeout = FILE_CONFIG.getInt(FILE_CONFIG_KEY_PREFIX + CONNECT_TIMEOUT_KEY, DEFAULT_CONNECT_TIMEOUT);
                    zkClient = new ZkClient(serverAddr, sessionTimeout, connectTimeout, zkSerializer);
                    String username = FILE_CONFIG.getConfig(FILE_CONFIG_KEY_PREFIX + AUTH_USERNAME);
                    String password = FILE_CONFIG.getConfig(FILE_CONFIG_KEY_PREFIX + AUTH_PASSWORD);
                    if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
                        StringBuilder auth = new StringBuilder(username).append(":").append(password);
                        zkClient.addAuthInfo("digest", auth.toString().getBytes());
                    }
                }
            }
            if (!zkClient.exists(ROOT_PATH)) {
                zkClient.createPersistent(ROOT_PATH, true);
            }
        }
    }

    @Override
    public String getTypeName() {
        return CONFIG_TYPE;
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        String value = getConfigFromSysPro(dataId);
        if (value != null) {
            return value;
        }
        FutureTask<String> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            String value1 = zkClient.readData(path);
            return StringUtils.isNullOrEmpty(value1) ? defaultValue : value1;
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("getConfig {} error or timeout, return defaultValue {}, exception:{} ",
                    dataId, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        FutureTask<Boolean> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            if (!zkClient.exists(path)) {
                zkClient.create(path, content, CreateMode.PERSISTENT);
            } else {
                zkClient.writeData(path, content);
            }
            return true;
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("putConfig {}, value: {} is error or timeout, exception: {}",
                    dataId, content, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        throw new NotSupportYetException("not support atomic operation putConfigIfAbsent");
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        FutureTask<Boolean> future = new FutureTask<>(() -> {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            return zkClient.delete(path);
        });
        CONFIG_EXECUTOR.execute(future);
        try {
            return future.get(timeoutMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("removeConfig {} is error or timeout, exception:{}", dataId, e.getMessage());
            return false;
        }
    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
        if (zkClient.exists(path)) {
            ZKListener zkListener = new ZKListener(path, listener);
            configListenersMap.computeIfAbsent(dataId, key -> new ConcurrentHashMap<>())
                    .put(listener, zkListener);
            zkClient.subscribeDataChanges(path, zkListener);
        }
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        Set<ConfigurationChangeListener> configChangeListeners = getConfigListeners(dataId);
        if (CollectionUtils.isNotEmpty(configChangeListeners)) {
            String path = ROOT_PATH + ZK_PATH_SPLIT_CHAR + dataId;
            if (zkClient.exists(path)) {
                for (ConfigurationChangeListener entry : configChangeListeners) {
                    if (listener.equals(entry)) {
                        ZKListener zkListener = null;
                        Map<ConfigurationChangeListener, ZKListener> configListeners = configListenersMap.get(dataId);
                        if (configListeners != null) {
                            zkListener = configListeners.get(listener);
                            configListeners.remove(entry);
                        }
                        if (zkListener != null) {
                            zkClient.unsubscribeDataChanges(path, zkListener);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        ConcurrentMap<ConfigurationChangeListener, ZKListener> configListeners = configListenersMap.get(dataId);
        if (CollectionUtils.isNotEmpty(configListeners)) {
            return configListeners.keySet();
        } else {
            return null;
        }
    }

    public static class ZKListener implements IZkDataListener {

        private String path;
        private ConfigurationChangeListener listener;

        public ZKListener(String path, ConfigurationChangeListener listener) {
            this.path = path;
            this.listener = listener;
        }

        @Override
        public void handleDataChange(String s, Object o) throws Exception {
            ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(s).setNewValue(o.toString())
                    .setChangeType(ConfigurationChangeType.MODIFY);
            listener.onProcessEvent(event);
        }

        @Override
        public void handleDataDeleted(String s) throws Exception {
            ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(s).setChangeType(
                    ConfigurationChangeType.DELETE);
            listener.onProcessEvent(event);
        }
    }

    private ZkSerializer getZkSerializer() {
        ZkSerializer zkSerializer = null;
        String serializer = FILE_CONFIG.getConfig(FILE_CONFIG_KEY_PREFIX + SERIALIZER_KEY);
        if (StringUtils.isNotBlank(serializer)) {
            try {
                Class<?> clazz = Class.forName(serializer);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                zkSerializer = (ZkSerializer) constructor.newInstance();
            } catch (ClassNotFoundException cfe) {
                LOGGER.warn("No zk serializer class found, serializer:{}", serializer, cfe);
            } catch (Throwable cause) {
                LOGGER.warn("found zk serializer encountered an unknown exception", cause);
            }
        }
        if (zkSerializer == null) {
            zkSerializer = new DefaultZkSerializer();
            LOGGER.info("Use default zk serializer: io.seata.config.zk.DefaultZkSerializer.");
        }
        return zkSerializer;
    }
}
