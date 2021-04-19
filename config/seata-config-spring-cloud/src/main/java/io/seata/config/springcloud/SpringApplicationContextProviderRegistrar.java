package io.seata.config.springcloud;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.seata.common.Constants.BEAN_NAME_SPRING_APPLICATION_CONTEXT_PROVIDER;

/**
 * @author: xielongfei
 * @date: 2021/03/29 10:42
 * @description:
 */
public class SpringApplicationContextProviderRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME_SPRING_APPLICATION_CONTEXT_PROVIDER)) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SpringApplicationContextProvider.class).getBeanDefinition();
            registry.registerBeanDefinition(BEAN_NAME_SPRING_APPLICATION_CONTEXT_PROVIDER, beanDefinition);
        }
    }
}
