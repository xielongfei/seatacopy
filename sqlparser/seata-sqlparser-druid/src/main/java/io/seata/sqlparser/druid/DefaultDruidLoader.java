package io.seata.sqlparser.druid;

import java.net.URL;

/**
 * @author: xielongfei
 * @date: 2021/05/08 17:22
 * @description:
 */
public class DefaultDruidLoader implements DruidLoader {
    @Override
    public URL getEmbeddedDruidLocation() {
        return null;
    }
}
