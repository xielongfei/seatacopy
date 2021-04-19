package io.seata.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: xielongfei
 * @date: 2021/03/20 20:56
 * @description:
 */
public class DataAccessExceptionTest {

    @Test
    public void testConstructorWithNoParameters() {
        exceptionAsserts(new DataAccessException());
    }

    private static void exceptionAsserts(DataAccessException exception) {
        assertThat(exception).isInstanceOf(DataAccessException.class).hasMessage(FrameworkErrorCode.UnknownAppError.getErrMessage());
        assertThat(exception.getErrcode()).isEqualTo(FrameworkErrorCode.UnknownAppError);
    }
}
