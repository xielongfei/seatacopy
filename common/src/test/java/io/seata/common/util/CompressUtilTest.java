package io.seata.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author: xielongfei
 * @date: 2021/03/24 10:36
 * @description:
 */
public class CompressUtilTest {

    @Test
    public void testCompress() throws IOException {
        byte[] bytes = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, 0,
                99, 100, 98, 6, 0, 29, -128, -68, 85, 3, 0, 0, 0};

        Assertions.assertArrayEquals(bytes,
                CompressUtil.compress(new byte[]{1, 2, 3}));
    }
}
