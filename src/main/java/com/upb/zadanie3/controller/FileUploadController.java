package com.upb.zadanie3.controller;

import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.storage.LocationConfig;
import com.upb.zadanie3.storage.StorageFileNotFoundException;
import com.upb.zadanie3.storage.StorageService;
import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.crypto.BadPaddingException;
import javax.crypto.ExemptionMechanismException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    private UserService userService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String mainController() {
        return "registration";
    }

    @GetMapping("/index")
    public String indexController() {
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

        model.addAttribute("users", userService.getAllUsers());
        return "uploadForm";
    }

    @GetMapping("/decrypt")
    public String decryptController() {
        return "decrypt";
    }

    @GetMapping("/{filename:.+}")
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

    @GetMapping("/decrypt/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveDecryptedFile(@PathVariable String filename) {

        Resource file = storageService.loadDecryptedFile(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/encrypt")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("username") String username,
                                   RedirectAttributes redirectAttributes) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        CryptoLogic cryptoLogic = new CryptoLogic();
        if(userService.getUserByUsername(username)==null){
            redirectAttributes.addFlashAttribute("message",
                    "No " + username + " user found in database");
        }
        else{
            User user = userService.getUserByUsername(username);
            cryptoLogic.loadPublicKey(user.getPublicKey());
            storageService.store(file, cryptoLogic);
            redirectAttributes.addFlashAttribute("message",
                    "File " + file.getOriginalFilename() + " has been encrypted successfully!");
        }
        return "redirect:./encrypt";
    }

    @PostMapping("/decrypt")
    public String handleDecryptFile(@RequestParam("file-to-decrypt") MultipartFile fileToDecrypt,
                                    @RequestParam("upload-secret") MultipartFile uploadSecretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        CryptoLogic cryptoLogic = new CryptoLogic();
        cryptoLogic.decrypt(fileToDecrypt,uploadSecretKey);
        return "redirect:./decrypt/"+fileToDecrypt.getOriginalFilename();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/decrypt/decApp")
    public ResponseEntity<Resource> downloadApp() throws IOException {
        File outputFile =  new File("src/app/DecryptApplication.zip");

        File jar = new File("src/app/DecryptApplication.jar");
        File key = new File("src/keys/private.key");
        List<File> files = new ArrayList<>();
        Collections.addAll(files, jar, key);

        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        for(File file : files) {
            zos.putNextEntry(new ZipEntry(file.getName()));

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 64];
            int len;
            while((len = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
        }

        zos.close();
        Resource resource = new UrlResource(outputFile.toURI());
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @GetMapping("/error")
    @ResponseBody
    public String errorController() {
        return "error";
    }
}