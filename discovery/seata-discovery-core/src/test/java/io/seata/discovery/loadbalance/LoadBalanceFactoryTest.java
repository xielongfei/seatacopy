package io.seata.discovery.loadbalance;

import io.seata.discovery.registry.RegistryFactory;
import io.seata.discovery.registry.RegistryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author: xielongfei
 * @date: 2021/04/19 17:46
 * @description:
 */
public class LoadBalanceFactoryTest {

    private static final String XID = "XID";

    /**
     * Test get registry.
     *
     * @param loadBalance the load balance
     * @throws Exception the exception
     */
    @ParameterizedTest
    @MethodSource("instanceProvider")
    @Disabled
    public void testGetRegistry(LoadBalance loadBalance) throws Exception {
        Assertions.assertNotNull(loadBalance);
        RegistryService registryService = RegistryFactory.getInstance();
        InetSocketAddress address1 = new InetSocketAddress("127.0.0.1", 8091);
        InetSocketAddress address2 = new InetSocketAddress("127.0.0.1", 8092);
        registryService.register(address1);
        registryService.register(address2);
        List<InetSocketAddress> addressList = registryService.lookup("my_test_tx_group");
        InetSocketAddress balanceAddress = loadBalance.select(addressList, XID);
        Assertions.assertNotNull(balanceAddress);
    }

    /**
     * Instance provider object [ ] [ ].
     *
     * @return the object [ ] [ ]
     */
    static Stream<Arguments> instanceProvider() {
        LoadBalance loadBalance = LoadBalanceFactory.getInstance();
        return Stream.of(
                Arguments.of(loadBalance)
        );
    }
}
