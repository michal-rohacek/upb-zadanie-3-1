package com.upb.zadanie3.storage;

import com.upb.zadanie3.security.CryptoLogic;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.NoSuchPaddingException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file, CryptoLogic cryptoLogic) throws NoSuchAlgorithmException, NoSuchPaddingException;

    Stream<Path> loadAll(Path location);

    Resource loadFiles(String filename);

    Resource loadKeys(String filename);

    Resource loadDecryptedFile(String filename);

    void deleteAll();

}
