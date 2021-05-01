package io.seata.core.context;

import io.seata.common.loader.EnhancedServiceLoader;

import java.util.Optional;

/**
 * @author: xielongfei
 * @date: 2021/04/30 14:36
 * @description:
 */
public class ContextCoreLoader {

    private ContextCoreLoader() {

    }

    private static class ContextCoreHolder {
        private static final ContextCore INSTANCE = Optional.ofNullable(EnhancedServiceLoader.load(ContextCore.class)).orElse(new ThreadLocalContextCore());
    }

    /**
     * Load context core.
     *
     * @return the context core
     */
    public static ContextCore load() {
        return ContextCoreHolder.INSTANCE;
    }
}
