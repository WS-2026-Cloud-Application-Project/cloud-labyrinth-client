package com.uni.labyrinth.service;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public   class ImageCache {
    private static  Map<String, Image> CACHE = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(GameServerService.class);

    private ImageCache() {}

    public static Image get(Class<?> clazz, String path) {
        return CACHE.computeIfAbsent(path, p -> {
            URL url = clazz.getResource(p);
            if (url == null) {
                log.info("ImageCache:get Image not found: " + path);
                throw new IllegalArgumentException("Image resource not found: " + p);
            }
            // backgroundLoading = true kann UI flüssiger machen (optional)
            return new Image(url.toExternalForm(), true);
        });
    }

    public static void clear() {
        CACHE.clear();
    }
}
