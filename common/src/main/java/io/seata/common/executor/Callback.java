package io.seata.common.executor;

/**
 * @author: xielongfei
 * @date: 2021/03/18 15:48
 * @description:
 */
public interface Callback<T> {

    T execute() throws Throwable;
}
