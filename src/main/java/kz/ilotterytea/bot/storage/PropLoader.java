package kz.ilotterytea.bot.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties loader.
 * @author ilotterytea
 * @since 1.0
 */
public class PropLoader extends Properties {

    public PropLoader(String file_path) {
        super();
        Logger LOGGER = LoggerFactory.getLogger(PropLoader.class);

        if (new File(file_path).exists()) {
            try {
                FileInputStream fis = new FileInputStream(file_path);
                super.load(fis);
                fis.close();
            } catch (IOException e) {
                LOGGER.error(String.format("Error occurred while loading the %s properties file", file_path), e);
            }
        }
    }
}
