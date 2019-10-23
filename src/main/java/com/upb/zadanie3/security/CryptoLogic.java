package com.upb.zadanie3.security;

import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoLogic {
    private static final int IV_SIZE = 16;
    private static final int KEY_SIZE = 16;
    private static final int AES_KEY_SIZE = 128;
    private static final int RSA_KEY_SIZE = 2048;
    private static int ENCRYPTED_SYMMETRIC_KEY_SIZE = 0;

    private Cipher cipherSymmetric;
    private Cipher cipherAsymmetric;
    private SecretKey secretKey;
    private GCMParameterSpec gcmParamSpec;
    private PKCS8EncodedKeySpec pkcs8KeySpec;
    private X509EncodedKeySpec x509EncodedKeySpec;
    private KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyFactory keyFactory;
    private byte[] ivBytes = new byte[IV_SIZE];
    private byte[] keyBytes = new byte[KEY_SIZE];
    private byte[] publicKeyBytes = new byte[RSA_KEY_SIZE];
    private byte[] privateKeyBytes = new byte[RSA_KEY_SIZE];
    private byte[] cipherText;
    private byte[] plainText;

    public CryptoLogic() throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.cipherSymmetric = Cipher.getInstance("AES/GCM/NoPadding");
        this.cipherAsymmetric = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    }

    public void generateKeyPair() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Path pathPrivateKey = Paths.get("src/keys/private.key");
        Path pathPublicKey = Paths.get("src/keys/public.key");
        if (Files.exists(pathPrivateKey) || Files.exists(pathPublicKey)) {
            loadKeyPair();
            return;
        }
        this.keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        this.keyPairGenerator.initialize(RSA_KEY_SIZE);
        this.keyPair = this.keyPairGenerator.generateKeyPair();
        this.privateKey = this.keyPair.getPrivate();
        this.publicKey = this.keyPair.getPublic();
        FileOutputStream outputStream = new FileOutputStream(new File("src/keys/private.key"));
        outputStream.write(this.privateKey.getEncoded());
        outputStream.close();
        outputStream = new FileOutputStream(new File("src/keys/public.key"));
        outputStream.write(this.publicKey.getEncoded());
        outputStream.close();
    }

    public void loadKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File privateKeyFile = new File("src/keys/private.key");
        File publicKeyFile = new File("src/keys/public.key");

        FileInputStream fileInputStream = new FileInputStream(privateKeyFile);
        this.privateKeyBytes = new byte[(int) privateKeyFile.length()];
        fileInputStream.read(this.privateKeyBytes);
        fileInputStream.close();

        fileInputStream = new FileInputStream(publicKeyFile);
        this.publicKeyBytes = new byte[(int) publicKeyFile.length()];
        fileInputStream.read(this.publicKeyBytes);
        fileInputStream.close();

        this.pkcs8KeySpec = new PKCS8EncodedKeySpec(this.privateKeyBytes);
        this.keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = this.keyFactory.generatePrivate(this.pkcs8KeySpec);

        this.x509EncodedKeySpec = new X509EncodedKeySpec(this.publicKeyBytes);
        this.publicKey = this.keyFactory.generatePublic(this.x509EncodedKeySpec);
    }
    public void loadPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Path pathPublicKey = Paths.get("src/keys/public.key");
        File publicKeyFile = new File(pathPublicKey.toString());
        FileInputStream fileInputStream = new FileInputStream(publicKeyFile);
        this.publicKeyBytes = new byte[(int) publicKeyFile.length()];
        fileInputStream.read(this.publicKeyBytes);
        fileInputStream.close();

        this.x509EncodedKeySpec = new X509EncodedKeySpec(this.publicKeyBytes);
        this.keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = this.keyFactory.generatePublic(this.x509EncodedKeySpec);

    }

    public void loadPublicKey(MultipartFile publicKeyFile ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File publicKeyFileTransfer = multipartToFile(publicKeyFile,"public.key");
        File publicKeyToSave = new File("src/keys/public.key");
        copyFileUsingStream(publicKeyFileTransfer,publicKeyToSave);

        FileInputStream fileInputStream = new FileInputStream(publicKeyFileTransfer);
        this.publicKeyBytes = new byte[(int) publicKeyFileTransfer.length()];
        fileInputStream.read(this.publicKeyBytes);
        fileInputStream.close();

        this.x509EncodedKeySpec = new X509EncodedKeySpec(this.publicKeyBytes);
        this.keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = this.keyFactory.generatePublic(this.x509EncodedKeySpec);

    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }


    private static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
        multipart.transferTo(convFile);
        return convFile;
    }

    public void encrypt(InputStream inputStream, File fOut, int fileSize) throws NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        OutputStream out = new FileOutputStream(fOut);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        this.secretKey = keyGenerator.generateKey();

        // Symmetric encryption params
        this.ivBytes = generateIVBytes();
        this.gcmParamSpec = new GCMParameterSpec(IV_SIZE * 8, this.ivBytes); // IV 96 up to 128 bits in GCM mode
        this.cipherSymmetric.init(Cipher.ENCRYPT_MODE, this.secretKey, this.gcmParamSpec);

        // Asymmetric encryption params

        this.cipherAsymmetric.init(Cipher.ENCRYPT_MODE, this.publicKey);

        this.plainText = new byte[fileSize];
        //FileInputStream inputStream = new FileInputStream(fIn);
        inputStream.read(this.plainText);
        inputStream.close();

        out.write(this.ivBytes);
        //out.write(this.publicKey.getEncoded());
        byte[] encryptedSymmetricKey = this.cipherAsymmetric.doFinal(this.secretKey.getEncoded());
        ENCRYPTED_SYMMETRIC_KEY_SIZE = encryptedSymmetricKey.length;
        out.write(encryptedSymmetricKey);
        out.write(this.cipherSymmetric.doFinal(plainText));


    }

    public void decrypt(MultipartFile inputFile, MultipartFile inputPrivateKeyFile) throws IOException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
        File fIn = multipartToFile(inputFile,inputFile.getName());
        File privateKeyFile = multipartToFile(inputPrivateKeyFile,"input-private-key");
        String finalFileName;
        if (fIn.getName().lastIndexOf(".") == -1) {
            finalFileName = fIn.getName();
        } else {
            String extension = fIn.getName().substring(fIn.getName().lastIndexOf("."));
            finalFileName = fIn.getName() + extension;
        }
        File decryptedFile = new File("src/decrypted/" + finalFileName);
        FileInputStream inputStream = new FileInputStream(fIn);

        this.privateKeyBytes = new byte[(int) privateKeyFile.length()];
        FileInputStream inPKStream = new FileInputStream(privateKeyFile);
        inPKStream.read(this.privateKeyBytes);


        // Extract IV
        this.ivBytes = new byte[IV_SIZE];
        inputStream.read(this.ivBytes);

        // Decrypt symmetric key
        this.pkcs8KeySpec = new PKCS8EncodedKeySpec(this.privateKeyBytes);
        this.keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = this.keyFactory.generatePrivate(this.pkcs8KeySpec);
        this.cipherAsymmetric = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        this.cipherAsymmetric.init(Cipher.DECRYPT_MODE, privateKey);
        this.keyBytes = new byte[ENCRYPTED_SYMMETRIC_KEY_SIZE];
        inputStream.read(this.keyBytes);
        byte[] decryptedKeyBytes = this.cipherAsymmetric.doFinal(this.keyBytes);

        // Init symmetric cipher algorithm with the decrypted symmetric key
        this.gcmParamSpec = new GCMParameterSpec(IV_SIZE * 8, this.ivBytes);
        this.secretKey = new SecretKeySpec(decryptedKeyBytes, 0, decryptedKeyBytes.length, "AES");
        this.cipherSymmetric.init(Cipher.DECRYPT_MODE, this.secretKey, this.gcmParamSpec);

        // Extract ciphertext
        this.cipherText = new byte[(int) (fIn.length() - (ENCRYPTED_SYMMETRIC_KEY_SIZE + IV_SIZE))];
        inputStream.read(this.cipherText);
        inputStream.close();

        FileOutputStream out = new FileOutputStream(decryptedFile);
        out.write(this.cipherSymmetric.doFinal(this.cipherText));
        out.close();


    }

    private byte[] generateIVBytes() throws NoSuchAlgorithmException {
        byte[] ivBytes = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(ivBytes);
        return ivBytes;
    }
}
