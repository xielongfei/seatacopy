package io.seata.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author: xielongfei
 * @date: 2021/03/25 15:44
 * @description:
 */
public class FileConfigurationTest {

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void addConfigListener() throws InterruptedException {
        Configuration fileConfig = ConfigurationFactory.getInstance();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        boolean value = fileConfig.getBoolean("service.disableGlobalTransaction");
        //FileListener.onChangeEvent 监听线程每秒执行一次
        fileConfig.addConfigListener("service.disableGlobalTransaction", (event) -> {
            Assertions.assertEquals(Boolean.parseBoolean(event.getNewValue()), !Boolean.parseBoolean(event.getOldValue()));
            countDownLatch.countDown();
        });
        System.setProperty("service.disableGlobalTransaction", String.valueOf(!value));
        Assertions.assertTrue(countDownLatch.await(2000, TimeUnit.MILLISECONDS));
    }
}
