package io.seata.discovery.loadbalance;

import io.seata.common.loader.LoadLevel;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.seata.discovery.loadbalance.LoadBalanceFactory.RANDOM_LOAD_BALANCE;

/**
 * @author: xielongfei
 * @date: 2021/04/19 15:49
 * @description:
 */
@LoadLevel(name = RANDOM_LOAD_BALANCE)
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected <T> T doSelect(List<T> invokers, String xid) {
        int length = invokers.size();
        return invokers.get(ThreadLocalRandom.current().nextInt(length));
    }
}
