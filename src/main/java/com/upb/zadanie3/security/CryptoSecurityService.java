package com.upb.zadanie3.security;

import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class CryptoSecurityService implements SecurityService {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    @Override
    public OutputStream getEncryptedOutputStream(String targetFilePath, String secretKeyString) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException {
        return getOutputStreamByCipherMode(targetFilePath, secretKeyString, Cipher.ENCRYPT_MODE);
    }

    @Override
    public OutputStream getDecryptedOutputStream(String targetFilePath, String secretKeyString) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException {
        return getOutputStreamByCipherMode(targetFilePath, secretKeyString, Cipher.DECRYPT_MODE);
    }

    private OutputStream getOutputStreamByCipherMode(String targetFilePath, String secretKeyString, int cipherMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, FileNotFoundException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(cipherMode, stringToSecretKey(secretKeyString), new IvParameterSpec(new byte[16]));

        return new CipherOutputStream(new FileOutputStream(targetFilePath), cipher);
    }

    @Override
    public String generateSecretKeyString() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return secretKeyToString(keyGen.generateKey());
    }

    private String secretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    private SecretKey stringToSecretKey(String secretKeyString) {
        byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }




}
