package io.seata.discovery.loadbalance;

import io.seata.common.rpc.RpcStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author: xielongfei
 * @date: 2021/04/07 18:17
 * @description:
 */
public class LoadBalanceTest {

    private static final String XID = "XID";

    /**
     *ParameterizedTest 表示该方法是一个参数化测试
     * MethodSource 引用测试类或外部类的一个或多个工厂方法
     */
    @ParameterizedTest
    @MethodSource("addressProvider")
    public void testLeastActiveLoadBalance_select(List<InetSocketAddress> addresses) throws Exception {
        int runs = 10000;
        int size = addresses.size();
        for (int i = 0; i<size -1; i++) {
            RpcStatus.beginCount(addresses.get(i).toString());
        }
        InetSocketAddress socketAddress = addresses.get(size - 1);
        LoadBalance loadBalance = new LeastActiveLoadBalance();
        for (int i = 0; i < runs; i++) {
            InetSocketAddress selectAddress = loadBalance.select(addresses, XID);
            Assertions.assertEquals(selectAddress, socketAddress);
        }
        RpcStatus.beginCount(socketAddress.toString());
        RpcStatus.beginCount(socketAddress.toString());
        Map<InetSocketAddress, AtomicLong> counter = getSelectedCounter(runs, addresses, loadBalance);
        for (InetSocketAddress address : counter.keySet()) {
            Long count = counter.get(address).get();
            if (address == socketAddress) {
                Assertions.assertEquals(count, 0);
            } else {
                Assertions.assertTrue(count > 0);
            }
        }
    }

    /**
     * Gets selected counter.
     *
     * @param runs        the runs
     * @param addresses   the addresses
     * @param loadBalance the load balance
     * @return the selected counter
     */
    public Map<InetSocketAddress, AtomicLong> getSelectedCounter(int runs, List<InetSocketAddress> addresses,
                                                                 LoadBalance loadBalance) {
        Assertions.assertNotNull(loadBalance);
        Map<InetSocketAddress, AtomicLong> counter = new ConcurrentHashMap<>();
        for (InetSocketAddress address : addresses) {
            counter.put(address, new AtomicLong(0));
        }
        try {
            for (int i = 0; i < runs; i++) {
                InetSocketAddress selectAddress = loadBalance.select(addresses, XID);
                counter.get(selectAddress).incrementAndGet();
            }
        } catch (Exception e) {
            //do nothing
        }
        return counter;
    }

    /**
     * Address provider object [ ] [ ].
     *
     * @return Stream<List < InetSocketAddress>>
     */
    static Stream<List<InetSocketAddress>> addressProvider() {
        return Stream.of(
                Arrays.asList(new InetSocketAddress("127.0.0.1", 8091),
                        new InetSocketAddress("127.0.0.1", 8092),
                        new InetSocketAddress("127.0.0.1", 8093),
                        new InetSocketAddress("127.0.0.1", 8094),
                        new InetSocketAddress("127.0.0.1", 8095))
        );
    }
}
