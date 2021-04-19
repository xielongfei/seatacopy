package io.seata.config.file;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.seata.common.loader.LoadLevel;
import io.seata.common.loader.Scope;
import io.seata.config.FileConfigFactory;
import io.seata.config.FileConfiguration;

import java.io.File;

/**
 * @author: xielongfei
 * @date: 2021/03/24 14:15
 * @description:
 */
@LoadLevel(name = FileConfigFactory.DEFAULT_TYPE, scope = Scope.PROTOTYPE)
public class SimpleFileConfig implements FileConfig {

    private Config fileConfig;

    public SimpleFileConfig() {
        fileConfig = ConfigFactory.load();
    }

    public SimpleFileConfig(File file, String name) {
        if (name.startsWith(FileConfiguration.SYS_FILE_RESOURCE_PREFIX)) {
            Config appConfig = ConfigFactory.parseFileAnySyntax(file);
            fileConfig = ConfigFactory.load(appConfig);
        } else {
            fileConfig = ConfigFactory.load(file.getName());
        }
    }

    @Override
    public String getString(String path) {
        return fileConfig.getString(path);
    }
}
