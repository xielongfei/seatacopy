package io.seata.config.springcloud;

import io.seata.common.holder.ObjectHolder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static io.seata.common.Constants.OBJECT_KEY_SPRING_APPLICATION_CONTEXT;

/**
 * @author: xielongfei
 * @date: 2021/03/29 10:46
 * @description:
 */
public class SpringApplicationContextProvider implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ObjectHolder.INSTANCE.setObject(OBJECT_KEY_SPRING_APPLICATION_CONTEXT, applicationContext);
    }
}
