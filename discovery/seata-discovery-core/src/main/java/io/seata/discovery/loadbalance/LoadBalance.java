package io.seata.discovery.loadbalance;

import java.util.List;

/**
 * @author: xielongfei
 * @date: 2021/04/07 16:14
 * @description:
 */
public interface LoadBalance {

    /**
     * Select t.
     *
     * @param <T>      the type parameter
     * @param invokers the invokers
     * @param xid      the xid
     * @return the t
     * @throws Exception the exception
     */
    <T> T select(List<T> invokers, String xid) throws Exception;
}
