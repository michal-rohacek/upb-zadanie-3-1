package com.upb.zadanie3.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    @Override
    public void transfer(InputStream is, OutputStream os) throws IOException {
        bufferedTransferFromIStoOS(is, os, 64);
        is.close();
        os.close();
    }

    private void bufferedTransferFromIStoOS(InputStream is, OutputStream os, int bufferSizeInKB) throws IOException {
        byte[] buffer = new byte[1024 * bufferSizeInKB];
        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

    }

    @Override
    public void saveSecretKeyToFile(String secretKeyString, String targetFilePath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(targetFilePath);
        fileOutputStream.write(secretKeyString.getBytes());
        fileOutputStream.close();
    }

    @Override
    public Stream<Path> loadAll(String subPath) throws IOException {
            return Files.walk(Paths.get(subPath), 1)
                .filter(path -> !path.equals(Paths.get(subPath)))
                .map(Paths.get(subPath)::relativize);
    }

    @Override
    public Path load(String filename) {
        return Paths.get("" + filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll(String subPath) {
        FileSystemUtils.deleteRecursively(Paths.get(subPath).toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(Paths.get(StoragePaths.ROOT_DIR));
            Files.createDirectories(Paths.get(StoragePaths.ENCRYPTED_DIR));
            Files.createDirectories(Paths.get(StoragePaths.DECRYPTED_DIR));
            Files.createDirectories(Paths.get(StoragePaths.SECRET_KEYS_DIR));
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
