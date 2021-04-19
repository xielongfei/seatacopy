package io.seata.common.util;

import org.junit.jupiter.api.Test;

/**
 * @author: xielongfei
 * @date: 2021/04/16 16:28
 * @description:
 */
public class IdWorkerTest {

    private static final int UUID_GENERATE_COUNT = 5;
    private static final Long SERVER_NODE_ID = 1023L;

    @Test
    public void testInitUUID() {
        IdWorker.init(SERVER_NODE_ID);
        for (int i = 0; i < UUID_GENERATE_COUNT; i++) {
            System.out.println("[UUID " + i + "] is: " + IdWorker.getInstance().nextId());
        }
    }

    @Test
    public void testUUID() {
        for (int i = 0; i < UUID_GENERATE_COUNT; i++) {
            System.out.println("[UUID " + i + "] is: " + IdWorker.getInstance().nextId());
        }
    }
}
