package io.seata.common.loader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author: xielongfei
 * @date: 2021/03/21 20:11
 * @description:
 */
public class EnhancedServiceLoaderTest {

    @Test
    public void testLoadByClassAndClassLoader() {
        Hello load = EnhancedServiceLoader.load(Hello.class, Hello.class.getClassLoader());
        Assertions.assertEquals(load.say(), "Olá.");
    }

    @Test
    public void testLoadException() {
        Assertions.assertThrows(EnhancedServiceNotFoundException.class, () -> {
            EnhancedServiceLoaderTest load = EnhancedServiceLoader.load(EnhancedServiceLoaderTest.class);
        });
    }

    /**
     * AppClassLoader 应用类加载器,又称为系统类加载器
     * ExtClassLoader 扩展类加载器 默认加载JAVA_HOME/jre/lib/ext/目录下的所有jar包
     *          ExtClassLoader初始化过程中，将父类加载器设置成了null, 因为 BootstrapClassLoader是 C++编写，对于 Java 本身来说它是不存在的。
     *          所以此处设置null表示它的父类加载器就是启动类加载器
     * BootstrapClassLoader 启动类加载器 顶层的类加载器 JDK中的核心ClassLoader类库，如：rt.jar、resources.jar、charsets.jar等
     */
    @Test
    public void testLoadByClass() {
        Hello load = EnhancedServiceLoader.load(Hello.class);
        assertThat(load.say()).isEqualTo("Olá.");
    }

    @Test
    public void getAllExtensionClass1() {
        List<Class> allExtensionClass = EnhancedServiceLoader
                .getAllExtensionClass(Hello.class, ClassLoader.getSystemClassLoader());
        assertThat(allExtensionClass).isNotEmpty();
    }

}
