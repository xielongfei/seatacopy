package io.seata.common.rpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author: xielongfei
 * @date: 2021/03/22 10:15
 * @description:
 */
public class RpcStatusTest {

    public static final String SERVICE = "127.0.0.1:80";

    @Test
    public void getStatus() {
        RpcStatus rpcStatus1 = RpcStatus.getStatus(SERVICE);
        Assertions.assertNotNull(rpcStatus1);
        RpcStatus rpcStatus2 = RpcStatus.getStatus(SERVICE);
        System.out.println(rpcStatus1.getClass() + " " + rpcStatus2.getClass());
        Assertions.assertEquals(rpcStatus1, rpcStatus2);
    }
}
