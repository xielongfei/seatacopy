package io.seata.discovery.loadbalance;

import io.seata.common.loader.LoadLevel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.seata.discovery.loadbalance.LoadBalanceFactory.ROUND_ROBIN_LOAD_BALANCE;

/**
 * @author: xielongfei
 * @date: 2021/04/19 15:52
 * @description:
 */
@LoadLevel(name = ROUND_ROBIN_LOAD_BALANCE)
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private final AtomicInteger sequence = new AtomicInteger();

    @Override
    protected <T> T doSelect(List<T> invokers, String xid) {
        int length = invokers.size();
        return invokers.get(getPositiveSequence() % length);
    }

    private int getPositiveSequence() {
        for (;;) {
            int current = sequence.get();
            int next = current >= Integer.MAX_VALUE ? 0 : current + 1;
            if (sequence.compareAndSet(current, next)) {
                return current;
            }
        }
    }
}
