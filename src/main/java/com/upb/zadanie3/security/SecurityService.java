package com.upb.zadanie3.security;

import javax.crypto.NoSuchPaddingException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface SecurityService {

    OutputStream getEncryptedOutputStream(String targetFilePath, String secretKeyString) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException;

    OutputStream getDecryptedOutputStream(String targetFilePath, String secretKeyString) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, FileNotFoundException;

    String generateSecretKeyString() throws NoSuchAlgorithmException;
}
