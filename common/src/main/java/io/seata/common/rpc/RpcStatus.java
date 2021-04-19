/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.common.rpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * The state statistics.
 *
 * @author ph3636
 */
public class RpcStatus {

    private static final ConcurrentMap<String, RpcStatus> SERVICE_STATUS_MAP = new ConcurrentHashMap<>();

    /**
     * AtomicLong中有个内部变量value保存着实际的long值，所有的操作都是针对该变量进行。
     * 也就是说，高并发环境下，value变量其实是一个热点，也就是N个线程竞争一个热点
     */
    private final AtomicLong active = new AtomicLong();

    /**
     * LongAdder基本思路就是 分散热点，将value值分散到一个数组中，不同线程会命中到不同槽中，各个线程只对自己
     * 槽中的那个值进行CAS操作，减少冲突。获取真正long值，只要将各个槽中的变量值累加返回。
     * ConcurrentHashMap中的“分段锁”其实就是类似的思路
     */
    private final LongAdder total = new LongAdder();

    private RpcStatus() {
    }

    /**
     * get the RpcStatus of this service
     *
     * @param service the service
     * @return RpcStatus
     */
    public static RpcStatus getStatus(String service) {
        return SERVICE_STATUS_MAP.computeIfAbsent(service, key -> new RpcStatus());
    }

    /**
     * remove the RpcStatus of this service
     *
     * @param service the service
     */
    public static void removeStatus(String service) {
        SERVICE_STATUS_MAP.remove(service);
    }

    /**
     * begin count
     *
     * @param service the service
     */
    public static void beginCount(String service) {
        getStatus(service).active.incrementAndGet();
    }

    /**
     * end count
     *
     * @param service the service
     */
    public static void endCount(String service) {
        RpcStatus rpcStatus = getStatus(service);
        rpcStatus.active.decrementAndGet();
        rpcStatus.total.increment();
    }

    /**
     * get active.
     *
     * @return active
     */
    public long getActive() {
        return active.get();
    }

    /**
     * get total.
     *
     * @return total
     */
    public long getTotal() {
        return total.longValue();
    }
}
