package com.upb.zadanie3.storage;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void transfer(InputStream is, OutputStream os) throws IOException;

    void saveSecretKeyToFile(String secretKeyString, String targetFilePath) throws IOException;

    Stream<Path> loadAll(String subPath) throws IOException;

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll(String subPath);

    void init();
}
