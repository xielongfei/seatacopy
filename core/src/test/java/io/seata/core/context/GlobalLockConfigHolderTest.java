package io.seata.core.context;

import io.seata.core.model.GlobalLockConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author: xielongfei
 * @date: 2021/05/03 22:21
 * @description:
 */
public class GlobalLockConfigHolderTest {

    /**
     * 在每个测试方法之前执行。
     * 注解在非静态方法上。
     * 可以重新初始化测试方法所需要使用的类的某些属性
     */
    @BeforeEach
    void setUp() {
        assertNull(GlobalLockConfigHolder.getCurrentGlobalLockConfig(), "should be null at first");
    }

    @Test
    void setAndReturnPrevious() {
        GlobalLockConfig config1 = new GlobalLockConfig();
        assertNull(GlobalLockConfigHolder.setAndReturnPrevious(config1), "should return null");
        assertSame(config1, GlobalLockConfigHolder.getCurrentGlobalLockConfig(), "holder fail to store config");

        GlobalLockConfig config2 = new GlobalLockConfig();
        assertSame(config1, GlobalLockConfigHolder.setAndReturnPrevious(config2), "fail to get previous config");
        assertSame(config2, GlobalLockConfigHolder.getCurrentGlobalLockConfig(), "holder fail to store latest config");
    }

    /**
     * 在每个测试方法之后执行。
     * 注解在非静态方法上。
     * 可以回滚测试方法引起的数据库修改。
     */
    @AfterEach
    void tearDown() {
        //采用相同的委托并断言它不会抛出异常
        assertDoesNotThrow(GlobalLockConfigHolder::remove, "clear method should not throw anything");
    }
}