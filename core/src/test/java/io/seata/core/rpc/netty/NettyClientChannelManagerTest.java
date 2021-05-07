package io.seata.core.rpc.netty;

import io.netty.channel.Channel;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author: xielongfei
 * @date: 2021/05/04 22:28
 * @description:
 * 在测试中使用Spring测试框架功能（例如）@MockBean，则必须使用@ExtendWith(SpringExtension.class)。它取代了不推荐使用的JUnit4@RunWith(SpringJUnit4ClassRunner.class)
 * 想涉及Mockito而不必涉及Spring，那么当您只想使用@Mock/ @InjectMocks批注时，您就想使用@ExtendWith(MockitoExtension.class)，因为它不会加载到很多不需要的Spring东西中。
 *   它替换了不推荐使用的JUnit4 @RunWith(MockitoJUnitRunner.class)。
 */
@ExtendWith(MockitoExtension.class)
public class NettyClientChannelManagerTest {

    private NettyClientChannelManager channelManager;

    @Mock
    private NettyPoolableFactory poolableFactory;

    @Mock
    private Function<String, NettyPoolKey> poolKeyFunction;

    private NettyClientConfig nettyClientConfig = new NettyClientConfig();

    @Mock
    private NettyPoolKey nettyPoolKey;

    @Mock
    private Channel channel;

    @Mock
    private Channel newChannel;

    @Mock
    private GenericKeyedObjectPool keyedObjectPool;

    @BeforeEach
    void setUp() {
        channelManager = new NettyClientChannelManager(poolableFactory, poolKeyFunction, nettyClientConfig);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void assertAcquireChannelFromPool() {
        setupPoolFactory(nettyPoolKey, channel);
        Channel actual = channelManager.acquireChannel("localhost");
        //检查是否发生了某些行为
        verify(poolableFactory).makeObject(nettyPoolKey);
        Assertions.assertEquals(actual, channel);
    }

    private void setupPoolFactory(final NettyPoolKey nettyPoolKey, final Channel channel) {
        //是对模拟对象的配置过程，为某些条件给定一个预期的返回值
        when(poolKeyFunction.apply(anyString())).thenReturn(nettyPoolKey);
        when(poolableFactory.makeObject(nettyPoolKey)).thenReturn(channel);
        when(poolableFactory.validateObject(nettyPoolKey, channel)).thenReturn(true);
    }
}
