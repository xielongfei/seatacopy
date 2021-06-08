package io.seata.rm;

import io.seata.core.context.GlobalLockConfigHolder;
import io.seata.core.context.RootContext;
import io.seata.core.model.GlobalLockConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author: xielongfei
 * @date: 2021/06/08
 * @description:
 */
public class GlobalLockTemplateTest {

    private final GlobalLockTemplate template = new GlobalLockTemplate();

    private final GlobalLockConfig config1 = generateGlobalLockConfig();

    private final GlobalLockConfig config2 = generateGlobalLockConfig();

    @BeforeEach
    void setUp() {
        RootContext.unbindGlobalLockFlag();
        GlobalLockConfigHolder.remove();
    }

    @Test
    void testSingle() {
        assertDoesNotThrow(() -> {
            template.execute(new GlobalLockExecutor() {
                @Override
                public Object execute() {
                    assertTrue(RootContext.requireGlobalLock(), "fail to bind global lock flag");
                    assertSame(config1, GlobalLockConfigHolder.getCurrentGlobalLockConfig(),
                            "global lock config changed during execution");
                    return null;
                }

                @Override
                public GlobalLockConfig getGlobalLockConfig() {
                    return config1;
                }
            });
        });
    }

    @AfterEach
    void tearDown() {
        assertFalse(RootContext.requireGlobalLock(), "fail to unbind global lock flag");
        assertNull(GlobalLockConfigHolder.getCurrentGlobalLockConfig(), "fail to clean global lock config");
    }

    private GlobalLockConfig generateGlobalLockConfig() {
        GlobalLockConfig config = new GlobalLockConfig();
        config.setLockRetryInternal(100);
        config.setLockRetryTimes(3);
        return config;
    }
}
