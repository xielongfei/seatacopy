package io.seata.discovery.registry.consul;

import io.seata.discovery.registry.RegistryService;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author: xielongfei
 * @date: 2021/04/20 11:19
 * @description:
 */
public class ConsulRegistryServiceImplTest {

    @Test
    public void testRegister() throws Exception {
        RegistryService registryService = mock(ConsulRegistryServiceImpl.class);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8091);
        registryService.register(inetSocketAddress);
        verify(registryService).register(inetSocketAddress);
    }

    @Test
    public void testUnregister() throws Exception {
        RegistryService registryService = mock(ConsulRegistryServiceImpl.class);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8091);
        registryService.unregister(inetSocketAddress);
        verify(registryService).unregister(inetSocketAddress);
    }

    @Test
    public void testSubscribe() throws Exception {
        RegistryService registryService = mock(ConsulRegistryServiceImpl.class);
        ConsulListener consulListener = mock(ConsulListener.class);
        registryService.subscribe("test", consulListener);
        verify(registryService).subscribe("test", consulListener);
    }

    @Test
    public void testLookup() throws Exception {
        RegistryService registryService = mock(ConsulRegistryServiceImpl.class);
        registryService.lookup("test-key");
        verify(registryService).lookup("test-key");
    }
}
