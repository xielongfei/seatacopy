package io.seata.common.loader;

import java.lang.annotation.*;

/**
 * @author: xielongfei
 * @date: 2021/03/18 15:59
 * @description:
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface LoadLevel {

    String name();

    int order() default 0;

    Scope scope() default Scope.SINGLETON;
}
