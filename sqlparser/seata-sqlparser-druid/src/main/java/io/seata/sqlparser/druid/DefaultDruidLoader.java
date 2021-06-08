package io.seata.sqlparser.druid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSource;

/**
 * @author: xielongfei
 * @date: 2021/05/08 17:22
 * @description:
 */
public class DefaultDruidLoader implements DruidLoader {
    /**
     * Default druid location in classpath
     */
    private final static String DRUID_LOCATION = "lib/sqlparser/druid.jar";

    private final static DefaultDruidLoader DRUID_LOADER = new DefaultDruidLoader(DRUID_LOCATION);

    private final URL druidUrl;

    /**
     * @param druidLocation druid location in classpath
     */
    private DefaultDruidLoader(String druidLocation) {
        if (druidLocation == null) {
            druidLocation = DRUID_LOCATION;
        }
        URL url = DefaultDruidLoader.class.getClassLoader().getResource(druidLocation);
        if (url != null) {
            try {
                File tempFile = File.createTempFile("seata", "sqlparser");
                try (InputStream input = url.openStream(); OutputStream output = new FileOutputStream(tempFile)){
                    byte[] buf = new byte[1024];
                    while (true) {
                        int readCnt = input.read(buf);
                        if (readCnt < 0) {
                            break;
                        }
                        output.write(buf, 0, readCnt);
                    }
                    output.flush();
                }
                tempFile.deleteOnExit();
                druidUrl = tempFile.toURI().toURL();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Class<?> clazz = null;
            try {
                clazz = DefaultDruidLoader.class.getClassLoader().loadClass("com.alibaba.druid.util.StringUtils");
            } catch (ClassNotFoundException ignore) {
            }
            if (clazz != null) {
                CodeSource cs = clazz.getProtectionDomain().getCodeSource();
                if (cs == null) {
                    throw new IllegalStateException("Can not find druid code source");
                }
                druidUrl = cs.getLocation();
            } else {
                druidUrl = null;
            }
        }
    }

    /**
     * Get default druid loader
     *
     * @return default druid loader
     */
    static DruidLoader get() {
        return DRUID_LOADER;
    }

    @Override
    public URL getEmbeddedDruidLocation() {
        if (druidUrl == null) {
            throw new IllegalStateException("Can not find embedded druid");
        }
        return druidUrl;

    }
}