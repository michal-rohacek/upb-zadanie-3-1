package com.upb.zadanie3.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.NoSuchPaddingException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file) throws NoSuchAlgorithmException, NoSuchPaddingException;

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
