package com.upb.zadanie3.controller;

import com.upb.zadanie3.security.SecurityService;
import com.upb.zadanie3.storage.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
public class FileUploadController {

    private StorageService storageService;
    private SecurityService securityService;

    public FileUploadController(StorageService storageService, SecurityService securityService) {
        this.storageService = storageService;
        this.securityService = securityService;
    }

    @PostMapping("/encrypt")
    public String handleFileEncryptionUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidAlgorithmParameterException, InvalidKeyException {

        String secretKeyString = securityService.generateSecretKeyString();
        String targetFilePath = StoragePaths.ENCRYPTED_DIR + file.getOriginalFilename();

        storageService.transfer(file.getInputStream(),
                securityService.getEncryptedOutputStream(targetFilePath, secretKeyString));

        String secretKeyFilePath = StoragePaths.SECRET_KEYS_DIR + "secret-key.txt";

        storageService.saveSecretKeyToFile(secretKeyString, secretKeyFilePath);

        redirectAttributes.addFlashAttribute("message",
                "Uspesne uploadnuty a encryptnuty subor " + file.getOriginalFilename() + "!");

        redirectAttributes.addFlashAttribute("secretKey", secretKeyString);
        redirectAttributes.addFlashAttribute("encryptedFileLink",
                MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", file.getOriginalFilename(), StoragePaths.ENCRYPTED_DIR).build().toString());

        return "redirect:./encrypt";
    }

    @PostMapping("/decrypt")
    public String handleFileDecryptionUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("secret-key") String secretKeyString,
                                   RedirectAttributes redirectAttributes) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {

            String targetFilePath = StoragePaths.DECRYPTED_DIR + file.getOriginalFilename();

            storageService.transfer(file.getInputStream(),
                    securityService.getDecryptedOutputStream(targetFilePath, secretKeyString));


        redirectAttributes.addFlashAttribute("message",
                "You successfully decrypted " + file.getOriginalFilename() + "!");

        redirectAttributes.addFlashAttribute("decryptedFileLink",
                MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", file.getOriginalFilename(), StoragePaths.DECRYPTED_DIR).build().toString());

        return "redirect:./decrypt";
    }


    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, String path) throws MalformedURLException {

        Resource file = new UrlResource(Paths.get(path + filename).toUri());

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/encrypt")
    public String listEncryptedFiles(Model model) throws IOException {
//        model.addAttribute("files", storageService.loadAll("files/encrypted/").map(
//                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                        "serveFile", path.getFileName().toString()).build().toString())
//                .collect(Collectors.toList()));
        return "encrypt";
    }

    @GetMapping("/decrypt")
    public String listDecryptedFiles(Model model) throws IOException {
//        model.addAttribute("file", storageService.loadAll(StoragePaths.DECRYPTED_DIR).map(
//                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                        "serveFile", path.getFileName().toString()).build().toString())
//                .collect(Collectors.toList()));
        return "decrypt";
    }

    @GetMapping("/index")
    public String index()  {
        return "index";
    }
}