package com.upb.zadanie3.controller;

import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.storage.LocationConfig;
import com.upb.zadanie3.storage.StorageFileNotFoundException;
import com.upb.zadanie3.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    private static final int RSA_KEY_SIZE = 4096;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String mainController() {
        return "index";
    }

    @GetMapping("/encrypt")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll(LocationConfig.filesLocation).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        model.addAttribute("keys", storageService.loadAll(LocationConfig.keysLocation).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveKey", path.getFileName().toString()).build().toString())
                .filter(p -> p.contains("public"))
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/decrypt")
    public String decryptController() {
        return "decrypt";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadFiles(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/keys/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveKey(@PathVariable String filename) {

        Resource file = storageService.loadKeys(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/encrypt")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("public-key-file") MultipartFile publicKeyFile,
                                   @RequestParam("encrypt-method") String encryptMethod,
                                   RedirectAttributes redirectAttributes) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        CryptoLogic cryptoLogic = new CryptoLogic();
        if (encryptMethod.equals("generate")) {
            cryptoLogic.generateKeyPair();
        } else if (encryptMethod.equals("upload-public")) {
            cryptoLogic.loadPublicKey(publicKeyFile);
        } else if (encryptMethod.equals("use-generated-key")) {
            cryptoLogic.loadPublicKey();
        }
        storageService.store(file, cryptoLogic);
        redirectAttributes.addFlashAttribute("message",
                "File " + file.getOriginalFilename() + " has been encrypted successfully!");

        return "redirect:/encrypt";
    }

    @PostMapping("/decrypt")
    public String handleDecryptFile(@RequestParam("file-to-decrypt") MultipartFile fileToDecrypt,
                                    @RequestParam("upload-secret") MultipartFile uploadSecretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        CryptoLogic cryptoLogic = new CryptoLogic();
        cryptoLogic.decrypt(fileToDecrypt,uploadSecretKey);
        return "redirect:/decrypt";
    }



    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/error")
    @ResponseBody
    public String errorController() {
        return "error";
    }

}