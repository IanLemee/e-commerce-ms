package com.tech.ian.stock.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class Loader {
    @Autowired
    private ResourceLoader loader;
    public String load(String path) throws IOException {
        var files = loader.getResource("classpath:%s".formatted(path)).getFile();
        return new String(Files.readAllBytes(files.toPath()));
    }
}
