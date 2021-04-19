package io.seata.common.thread;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: xielongfei
 * @date: 2021/03/22 18:12
 * @description:
 */
public class PositiveAtomicCounterTest {

    @Test
    public void testIncrementAndGet() {
        PositiveAtomicCounter counter = new PositiveAtomicCounter();
        assertThat(counter.incrementAndGet()).isEqualTo(1);
    }
}
