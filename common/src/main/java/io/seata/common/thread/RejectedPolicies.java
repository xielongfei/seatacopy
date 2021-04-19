package io.seata.common.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * @author: xielongfei
 * @date: 2021/03/19 17:59
 * @description:
 */
public final class RejectedPolicies {

    public static RejectedExecutionHandler runsOldestTaskPolicy() {
        return (r, executor) -> {
            if (executor.isShutdown()) {
                return;
            }
            BlockingQueue<Runnable> workQueue = executor.getQueue();
            //从队列头中删除元素，空集合返回null
            Runnable firstWork = workQueue.poll();
            //添加一个元素并返回true, 队列满返false
            boolean newTaskAdd = workQueue.offer(r);
            if (firstWork != null) {
                //System.out.println(Thread.currentThread().getName());
                //拒绝策略下，主线程执行run方法
                firstWork.run();
            }
            if (!newTaskAdd) {
                executor.execute(r);
            }
        };
    }
}
