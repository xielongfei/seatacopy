package io.seata.discovery.loadbalance;

import io.seata.common.loader.LoadLevel;
import io.seata.config.ConfigurationFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static io.seata.config.ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR;
import static io.seata.config.ConfigurationKeys.FILE_ROOT_REGISTRY;
import static io.seata.common.DefaultValues.VIRTUAL_NODES_DEFAULT;
import static io.seata.discovery.loadbalance.LoadBalanceFactory.CONSISTENT_HASH_LOAD_BALANCE;

/**
 * @author: xielongfei
 * @date: 2021/04/07 16:21
 * @description:
 */
@LoadLevel(name = CONSISTENT_HASH_LOAD_BALANCE)
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private static final String VIRTUAL_NODES = FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + "loadBalanceVirtualNodes";
    private static final int VIRTUAL_NODES_NUM = ConfigurationFactory.CURRENT_FILE_INSTANCE.getInt(VIRTUAL_NODES, VIRTUAL_NODES_DEFAULT);

    @Override
    protected <T> T doSelect(List<T> invokers, String xid) {
        return new ConsistentHashSelector<>(invokers, VIRTUAL_NODES_NUM).select(xid);
    }

    private static final class ConsistentHashSelector<T> {
        private final SortedMap<Long, T> virtualInvokers = new TreeMap<>();
        private final HashFunction hashFunction = new MD5Hash();

        ConsistentHashSelector(List<T> invokers, int virtualNodes) {
            for (T invoker : invokers) {
                for (int i = 0; i < virtualNodes; i++) {
                    virtualInvokers.put(hashFunction.hash(invoker.toString() + i), invoker);
                }
            }
        }

        public T select(String objectKey) {
            //tailMap(K fromKey) 方法用于返回此映射，其键大于或等于fromKey的部分视图
            SortedMap<Long, T> tailMap = virtualInvokers.tailMap(hashFunction.hash(objectKey));
            Long nodeHashVal = tailMap.isEmpty() ? virtualInvokers.firstKey() : tailMap.firstKey();
            return virtualInvokers.get(nodeHashVal);
        }
    }

    private static class MD5Hash implements HashFunction {
        MessageDigest instance;
        public MD5Hash() {
            try {
                instance = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        @Override
        public long hash(String key) {
            instance.reset();
            instance.update(key.getBytes());
            byte[] digest = instance.digest();
            long h = 0;
            for (int i = 0; i < 4; i++) {
                //左移赋值运算符
                h <<= 8;
                /**
                 *   | 或运算法 两位只要有一个为1，其值为1，其它都为0
                 *   & 与运算法 两位同时为1，结果才为1，否则为0
                 */
                h |= ((int) digest[i]) & 0xFF;
            }
            return h;
        }
    }

    /**
     * Hash String to long value
     */
    public interface HashFunction {
        long hash(String key);
    }
}
