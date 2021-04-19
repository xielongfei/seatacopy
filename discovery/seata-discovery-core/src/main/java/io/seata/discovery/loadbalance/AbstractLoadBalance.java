package io.seata.discovery.loadbalance;

import io.seata.common.util.CollectionUtils;

import java.util.List;

/**
 * @author: xielongfei
 * @date: 2021/04/07 16:17
 * @description:
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public <T> T select(List<T> invokers, String xid) {
        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return doSelect(invokers, xid);
    }

    /**
     * Do select t.
     *
     * @param <T>      the type parameter
     * @param invokers the invokers
     * @param xid      the xid
     * @return the t
     */
    protected abstract <T> T doSelect(List<T> invokers, String xid);

}
