package io.seata.common.holder;

import io.seata.common.exception.ShouldNeverHappenException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xielongfei
 * @date: 2021/03/18 15:50
 * @description:
 */
public enum ObjectHolder {

    /**
     * singleton instance
     */
    INSTANCE;
    private static final int MAP_SIZE = 8;
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>(MAP_SIZE);

    public Object getObject(String ObjectKey) {
        return OBJECT_MAP.get(ObjectKey);
    }

    public <T> T getObject(Class<T> clasz) {
        return clasz.cast(OBJECT_MAP.values().stream().filter(clasz::isInstance).findAny().orElseThrow(() -> new ShouldNeverHappenException("Can't find any object of class " + clasz.getName())));
    }

    public Object setObject(String objectKey, Object object) {
        return OBJECT_MAP.putIfAbsent(objectKey, object);
    }
}
