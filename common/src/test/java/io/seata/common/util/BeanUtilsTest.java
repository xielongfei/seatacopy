package io.seata.common.util;

import io.seata.common.BranchDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: xielongfei
 * @date: 2021/03/23 10:59
 * @description:
 */
public class BeanUtilsTest {

    /**
     * 按位与运算   &  两位同时为1，结果为1，否则为0
     * 按位或运算符 |  参加运算的两个对象只要有一个为1，其值为1
     * 异或运算符   ^  参加运算的两个对象，如果两个相应位为“异”（值不同），则该位结果为1，否则为0
     * 取反运算符   ~  对一个二进制数按位取反，即将0变1，1变0
     *
     * 0x7FFFFFFF是int的最大值  每个16进制数4bit, 因此8位16进制是4个字节，刚好是一个int整形
     *  F的二进制码为 1111
     *  7的二进制码为 0111
     *  整个0x7FFFFFFF的二进制表示除了首位是0（第一位是符号位，0表示正数），其余都是1
     */

    @Test
    public void testMapToObjectNotNull() {
        Map<String, String> map = new HashMap<>();
        Date date = new Date();
        map.put("xid", "192.166.166.11:9010:12423424234234");
        map.put("transactionId", "12423424234234");
        map.put("status", "2");
        map.put("test", "22.22");
        map.put("gmtCreate", String.valueOf(date.getTime()));
        BranchDO branchDO =
                (BranchDO) BeanUtils.mapToObject(map, BranchDO.class);
        Assertions.assertEquals(map.get("xid"), branchDO.getXid());
        Assertions.assertEquals(Long.valueOf(map.get("transactionId")), branchDO.getTransactionId());
        Assertions.assertEquals(Integer.valueOf(map.get("status")), branchDO.getStatus());
        Assertions.assertEquals(Double.valueOf(map.get("test")), branchDO.getTest());
        Assertions.assertEquals(new Date(date.getTime()), branchDO.getGmtCreate());
    }

}
