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
            //mystery if else
        }
        storageService.store(file, cryptoLogic);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/encrypt";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}