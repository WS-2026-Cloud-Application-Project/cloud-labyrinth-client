package com.uni.labyrinth.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Config {

    private static final Properties props = new Properties();

    static {
        Path external = Paths.get(System.getProperty("user.dir"))
                .resolve("application.properties");

        if (Files.exists(external)) {
            try (FileInputStream fis = new FileInputStream(external.toFile())) {
                props.load(fis);
                System.out.println("Loaded external config: " + external.toAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load external config: " + external.toAbsolutePath(), e);
            }
        } else {
            try (InputStream in = Config.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {

                if (in == null) {
                    throw new RuntimeException("No external application.properties and none in resources/classpath.");
                }

                props.load(in);
                System.out.println("Loaded config from resources (classpath)");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load application.properties from resources", e);
            }
        }
    }

    public static String get(String key) {
        String v = props.getProperty(key);
        if (v == null) throw new IllegalArgumentException("Missing property: " + key);
        return v;
    }
}
