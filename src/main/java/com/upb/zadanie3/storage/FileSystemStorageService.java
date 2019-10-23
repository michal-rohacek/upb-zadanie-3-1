package com.upb.zadanie3.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import com.upb.zadanie3.security.CryptoLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.stream.Location;

@Service
public class FileSystemStorageService implements StorageService {

    @Autowired
    public FileSystemStorageService() { }

    @Override
    public void store(MultipartFile file, CryptoLogic cryptoLogic) throws NoSuchAlgorithmException, NoSuchPaddingException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        File encrypted = new File("encrypted");
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory "
                                + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                cryptoLogic.encrypt(inputStream, encrypted, (int) file.getSize());
                Files.copy(new FileInputStream(encrypted), LocationConfig.filesLocation.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll(Path location) {
        try {
            return Files.walk(location, 1)
                    .filter(path -> !path.equals(location))
                    .map(location::relativize);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }


    @Override
    public Resource loadFiles(String filename) {
        Path file = LocationConfig.filesLocation.resolve(filename);
        return loadAsResource(file);
    }

    @Override
    public Resource loadKeys(String filename) {
        Path key = LocationConfig.keysLocation.resolve(filename);
        return loadAsResource(key);
    }

    private Resource loadAsResource(Path file) {
        try {
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: ");

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: ", e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(LocationConfig.filesLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(LocationConfig.filesLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
