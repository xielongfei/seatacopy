package io.seata.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author: xielongfei
 * @date: 2021/03/29 14:51
 * @description:
 */
public class CustomConfigurationTest {
    @Test
    public void testCustomConfigLoad() throws Exception {
        Configuration configuration = ConfigurationFactory.getInstance();
        Assertions.assertTrue(null != configuration);
        Properties properties;
        try (InputStream input = CustomConfigurationForTest.class.getClassLoader().getResourceAsStream("custom_for_test.properties")) {
            properties = new Properties();
            properties.load(input);
        }
        Assertions.assertNotNull(properties);
        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);
            Assertions.assertNotNull(value);
            Assertions.assertEquals(value, configuration.getConfig(name));
        }
    }
}
