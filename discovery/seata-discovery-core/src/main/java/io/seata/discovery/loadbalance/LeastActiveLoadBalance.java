package io.seata.discovery.loadbalance;

import io.seata.common.loader.LoadLevel;
import io.seata.common.rpc.RpcStatus;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import static io.seata.discovery.loadbalance.LoadBalanceFactory.LEAST_ACTIVE_LOAD_BALANCE;

/**
 * @author: xielongfei
 * @date: 2021/04/07 17:59
 * @description:
 */
@LoadLevel(name = LEAST_ACTIVE_LOAD_BALANCE)
public class LeastActiveLoadBalance extends AbstractLoadBalance {

    @Override
    protected <T> T doSelect(List<T> invokers, String xid) {
        int length = invokers.size();
        long leastActive = -1;
        int leastCount = 0;
        int[] leastIndexes = new int[length];
        for (int i = 0; i < length; i++) {
            long active = RpcStatus.getStatus(invokers.get(i).toString()).getActive();
            if (leastActive == -1 || active < leastActive) {
                leastActive = active;
                leastCount = 1;
                leastIndexes[0] = i;
            } else if (active == leastActive) {
                leastIndexes[leastCount++] = i;
            }
        }
        if (leastCount == 1) {
            return invokers.get(leastIndexes[0]);
        }
        return invokers.get(leastIndexes[ThreadLocalRandom.current().nextInt(leastCount)]);
    }

    public static void main(String[] args) {
        AtomicLong l1 = new AtomicLong();
        System.out.println(l1.get());
    }
}
