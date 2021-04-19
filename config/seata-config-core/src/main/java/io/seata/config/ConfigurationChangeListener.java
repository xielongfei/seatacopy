package io.seata.config;

import io.seata.common.thread.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: xielongfei
 * @date: 2021/03/24 14:56
 * @description:
 */
public interface ConfigurationChangeListener {

    /**
     * The constant CORE_LISTENER_THREAD.
     */
    int CORE_LISTENER_THREAD = 1;
    /**
     * The constant MAX_LISTENER_THREAD.
     */
    int MAX_LISTENER_THREAD = 1;
    /**
     * The constant EXECUTOR_SERVICE.
     */
    ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(CORE_LISTENER_THREAD, MAX_LISTENER_THREAD,
            Integer.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
            new NamedThreadFactory("configListenerOperate", MAX_LISTENER_THREAD));

    void onChangeEvent(ConfigurationChangeEvent event);

    default void onProcessEvent(ConfigurationChangeEvent event) {
        getExecutorService().submit(() -> {
           beforeEvent();
           onChangeEvent(event);
           afterEvent();
        });
    }

    default void onShutDown() {
        getExecutorService().shutdownNow();
    }

    default ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    default void beforeEvent(){

    }

    default void afterEvent() {

    }
}
