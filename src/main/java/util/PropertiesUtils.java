package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {
    private static final String propertiesPath = "\\src\\main\\resources\\application.properties";

    public static Properties GetProperties() {
        Properties props = new Properties();

        try(FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + propertiesPath)) {
            props.load(fis);
            return props;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
