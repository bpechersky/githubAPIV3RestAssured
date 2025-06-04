package utils;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static final Properties props = new Properties();
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            props.load(fis);
        } catch (IOException e) {
            System.out.println("config.properties not found, relying on .env and environment variables.");
        }
    }

    public static String get(String key) {
        String env = System.getenv(key.toUpperCase().replace(".", "_"));
        String dot = dotenv.get(key.toUpperCase().replace(".", "_"));
        return env != null ? env : (dot != null ? dot : props.getProperty(key));
    }
}