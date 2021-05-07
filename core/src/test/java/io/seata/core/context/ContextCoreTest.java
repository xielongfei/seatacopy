package io.seata.core.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The type Context core test.
 *
 * @author guoyao
 */
public class ContextCoreTest {

    private final String FIRST_KEY = "first_key";
    private final String FIRST_VALUE = "first_value";
    private final String SECOND_KEY = "second_key";
    private final String SECOND_VALUE = "second_value";
    private final String NOT_EXIST_KEY = "not_exist_key";

    /**
     * Map put函数
     * 对返回值的进一步解释：
     *  1.如果没有键映射，则返回NULL。
     *  2.该函数返回与指定键关联的旧值。
     *  3.这个操作不管啥条件都会覆盖旧的。
     */

    /**
     * Test put.
     */
    @Test
    public void testPut() {
        ContextCore load = ContextCoreLoader.load();
        Object object = load.put(FIRST_KEY, FIRST_VALUE);
        assertThat(load.put(FIRST_KEY, FIRST_VALUE)).isNull();
        assertThat(load.put(SECOND_KEY, SECOND_VALUE)).isNull();
        assertThat(load.put(FIRST_KEY, SECOND_VALUE)).isEqualTo(FIRST_VALUE);
        assertThat(load.put(SECOND_KEY, FIRST_VALUE)).isEqualTo(SECOND_VALUE);
        //clear keys
        load.remove(FIRST_KEY);
        load.remove(SECOND_KEY);
    }

    /**
     * Test get.
     */
    @Test
    public void testGet() {
        ContextCore load = ContextCoreLoader.load();
        load.put(FIRST_KEY, FIRST_VALUE);
        load.put(SECOND_KEY, FIRST_VALUE);
        assertThat(load.get(FIRST_KEY)).isEqualTo(FIRST_VALUE);
        assertThat(load.get(SECOND_KEY)).isEqualTo(FIRST_VALUE);
        load.put(FIRST_KEY, SECOND_VALUE);
        load.put(SECOND_KEY, SECOND_VALUE);
        assertThat(load.get(FIRST_KEY)).isEqualTo(SECOND_VALUE);
        assertThat(load.get(SECOND_KEY)).isEqualTo(SECOND_VALUE);
        assertThat(load.get(NOT_EXIST_KEY)).isNull();
        //clear keys
        load.remove(FIRST_KEY);
        load.remove(SECOND_KEY);
        load.remove(NOT_EXIST_KEY);
    }

    /**
     * Test remove.
     */
    @Test
    public void testRemove() {
        ContextCore load = ContextCoreLoader.load();
        load.put(FIRST_KEY, FIRST_VALUE);
        load.put(SECOND_KEY, SECOND_VALUE);
        assertThat(load.remove(FIRST_KEY)).isEqualTo(FIRST_VALUE);
        assertThat(load.remove(SECOND_KEY)).isEqualTo(SECOND_VALUE);
        assertThat(load.remove(NOT_EXIST_KEY)).isNull();
    }
}
