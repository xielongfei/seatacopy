package io.seata.discovery.registry.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.launcher.junit.EtcdClusterResource;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchResponse;
import io.seata.discovery.registry.RegistryService;
import io.seata.discovery.registry.etcd3.EtcdRegistryProvider;
import io.seata.discovery.registry.etcd3.EtcdRegistryServiceImpl;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.util.CharsetUtil.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: xielongfei
 * @date: 2021/04/22 10:08
 * @description:
 */

//禁用整个测试类或单个方法
@Disabled
public class EtcdRegistryServiceImplTest {
    private static final String REGISTRY_KEY_PREFIX = "registry-seata-";
    private static final String CLUSTER_NAME = "default";

    //提供了测试用例执行过程中一些通用功能的共享的能力
    @Rule
    private final static EtcdClusterResource etcd = new EtcdClusterResource(CLUSTER_NAME, 1);

    private final Client client = Client.builder().endpoints(etcd.cluster().getClientEndpoints()).build();
    private final static String HOST = "127.0.0.1";
    private final static int PORT = 8091;

    @BeforeAll
    public static void beforeClass() throws Exception {
        System.setProperty(EtcdRegistryServiceImpl.TEST_ENDPONT, etcd.cluster().getClientEndpoints().get(0).toString());
    }

    @AfterAll
    public static void afterClass() throws Exception {
        System.setProperty(EtcdRegistryServiceImpl.TEST_ENDPONT, "");
    }

    @Test
    public void testRegister() throws Exception {
        RegistryService registryService = new EtcdRegistryProvider().provide();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
        //1.register
        registryService.register(inetSocketAddress);
        //2.get instance information
        GetOption getOption = GetOption.newBuilder().withPrefix(buildRegistryKeyPrefix()).build();
        long count = client.getKVClient().get(buildRegistryKeyPrefix(), getOption).get().getKvs().stream().filter(keyValue -> {
            String[] instanceInfo = keyValue.getValue().toString(UTF_8).split(":");
            return HOST.equals(instanceInfo[0]) && PORT == Integer.parseInt(instanceInfo[1]);
        }).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testUnregister() throws Exception {
        RegistryService registryService = new EtcdRegistryProvider().provide();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
        //1.register
        registryService.register(inetSocketAddress);
        //2.get instance information
        GetOption getOption = GetOption.newBuilder().withPrefix(buildRegistryKeyPrefix()).build();
        long count = client.getKVClient().get(buildRegistryKeyPrefix(), getOption).get().getKvs().stream().filter(keyValue -> {
            String[] instanceInfo = keyValue.getValue().toString(UTF_8).split(":");
            return HOST.equals(instanceInfo[0]) && PORT == Integer.parseInt(instanceInfo[1]);
        }).count();
        assertThat(count).isEqualTo(1);
        //3.unregister
        registryService.unregister(inetSocketAddress);
        //4.again get instance information
        getOption = GetOption.newBuilder().withPrefix(buildRegistryKeyPrefix()).build();
        count = client.getKVClient().get(buildRegistryKeyPrefix(), getOption).get().getKvs().stream().filter(keyValue -> {
            String[] instanceInfo = keyValue.getValue().toString(UTF_8).split(":");
            return HOST.equals(instanceInfo[0]) && PORT == Integer.parseInt(instanceInfo[1]);
        }).count();
        assertThat(count).isEqualTo(0);


    }

    @Test
    public void testSubscribe() throws Exception {
        RegistryService registryService = new EtcdRegistryProvider().provide();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
        //1.register
        registryService.register(inetSocketAddress);
        //2.subscribe
        EtcdListener etcdListener = new EtcdListener();
        registryService.subscribe(CLUSTER_NAME, etcdListener);
        //3.delete instance,see if the listener can be notified
        DeleteOption deleteOption = DeleteOption.newBuilder().withPrefix(buildRegistryKeyPrefix()).build();
        client.getKVClient().delete(buildRegistryKeyPrefix(), deleteOption).get();
        assertThat(etcdListener.isNotified()).isTrue();
    }

    @Test
    public void testUnsubscribe() throws Exception {
        RegistryService registryService = new EtcdRegistryProvider().provide();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
        //1.register
        registryService.register(inetSocketAddress);
        //2.subscribe
        EtcdListener etcdListener = new EtcdListener();
        registryService.subscribe(CLUSTER_NAME, etcdListener);
        //3.delete instance,see if the listener can be notified
        DeleteOption deleteOption = DeleteOption.newBuilder().withPrefix(buildRegistryKeyPrefix()).build();
        client.getKVClient().delete(buildRegistryKeyPrefix(), deleteOption).get();
        assertThat(etcdListener.isNotified()).isTrue();
        //4.unsubscribe
        registryService.unsubscribe(CLUSTER_NAME, etcdListener);
        //5.reset
        etcdListener.reset();
        //6.put instance,the listener should not be notified
        client.getKVClient().put(buildRegistryKeyPrefix(), ByteSequence.from("test", UTF_8)).get();
        assertThat(etcdListener.isNotified()).isFalse();
    }

    @Test
    public void testLookup() throws Exception {
        RegistryService registryService = new EtcdRegistryProvider().provide();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
        //1.register
        registryService.register(inetSocketAddress);
        //2.lookup
        List<InetSocketAddress> inetSocketAddresses = registryService.lookup("my_test_tx_group");
        assertThat(inetSocketAddresses).size().isEqualTo(1);
    }

    /**
     * build registry key prefix
     *
     * @return
     */
    private ByteSequence buildRegistryKeyPrefix() {
        return ByteSequence.from(REGISTRY_KEY_PREFIX, UTF_8);
    }

    /**
     * etcd listener
     */
    private static class EtcdListener implements Watch.Listener {
        private boolean notified = false;

        @Override
        public void onNext(WatchResponse response) {
            notified = true;

        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }

        /**
         * @return
         */
        public boolean isNotified() throws InterruptedException {
            TimeUnit.SECONDS.sleep(3);
            return notified;
        }

        /**
         * reset
         */
        private void reset() {
            this.notified = false;
        }
    }

}
