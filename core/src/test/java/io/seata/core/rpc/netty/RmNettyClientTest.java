package io.seata.core.rpc.netty;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author: xielongfei
 * @date: 2021/05/04 22:53
 * @description:
 */
public class RmNettyClientTest {

    @Test
    public void assertGetInstanceAfterDestroy() {
        RmNettyRemotingClient oldClient = RmNettyRemotingClient.getInstance("ap", "group");
        AtomicBoolean initialized = getInitializeStatus(oldClient);
        oldClient.init();
        assertTrue(initialized.get());
        oldClient.destroy();
        assertFalse(initialized.get());
        RmNettyRemotingClient newClient = RmNettyRemotingClient.getInstance("ap", "group");
        Assertions.assertNotEquals(oldClient, newClient);
        initialized = getInitializeStatus(newClient);
        assertFalse(initialized.get());
        newClient.init();
        assertTrue(initialized.get());
        newClient.destroy();
    }

    private AtomicBoolean getInitializeStatus(final RmNettyRemotingClient rmNettyRemotingClient) {
        try {
            Field field = rmNettyRemotingClient.getClass().getDeclaredField("initialized");
            field.setAccessible(true);
            return (AtomicBoolean) field.get(rmNettyRemotingClient);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
