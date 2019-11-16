package com.upb.zadanie3.controller;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.storage.LocationConfig;
import com.upb.zadanie3.storage.StorageFileNotFoundException;
import com.upb.zadanie3.storage.StorageService;
import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@PreAuthorize("isAuthenticated()")
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    private UserService userService;

    @Autowired
    private IFileRepository fileRepository;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/index")
    public String indexController() {
        return "index";
    }

    @GetMapping("/encrypt")
    public String listUploadedFiles(Model model) throws IOException {
        List<String> names = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            names.add(user.getUsername());
        }
        model.addAttribute("users", names);
        return "encrypt";
    }

    @GetMapping("/decrypt")
    public String decryptController() {

        System.out.println("SOME STRING: " +getCurrentPrincipal().getSomeString());
        System.out.println("USERname from principal: " + getCurrentPrincipal().getUsername());
        System.out.println("USER from prinC:" + getCurrentPrincipal().getUser());
        return "decrypt";
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
        if (userService.getUserByUsername(username) == null) {
            redirectAttributes.addFlashAttribute("message",
                    "No " + username + " user found in database");
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("valid", false);
        } else {
            User user = userService.getUserByUsername(username);
            cryptoLogic.loadPublicKey(user.getPublicKey());
            storageService.store(file, cryptoLogic);
            EncryptedFile encFile = new EncryptedFile();
            encFile.setRecipientUser(user);
            encFile.setFileName(file.getOriginalFilename());
            fileRepository.save(encFile);
            redirectAttributes.addFlashAttribute("message",
                    "File " + file.getOriginalFilename() + " has been encrypted successfully!");
            redirectAttributes.addFlashAttribute("valid", true);
            redirectAttributes.addFlashAttribute("error", false);
        }
        return "redirect:./encrypt";
    }

    @PostMapping("/decrypt")
    public String handleDecryptFile(@RequestParam("file-to-decrypt") MultipartFile fileToDecrypt
    ) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeyException, IOException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        CryptoLogic cryptoLogic = new CryptoLogic();
        cryptoLogic.decrypt(fileToDecrypt, userService);
        return "redirect:./decrypt/" + fileToDecrypt.getOriginalFilename();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/decrypt/decApp")
    public ResponseEntity<Resource> downloadApp() throws IOException {
        File outputFile = new File("src/app/DecryptApplication.zip");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((UserPrincipal) principal).getUsername();
        User logedUser = userService.getUserByUsername(userId);
        byte[] privateKeyBytes = Base64.getDecoder().decode(logedUser.getPrivateKey());

        File jar = new File("src/app/DecryptApplication.jar");
        File key = new File("src/keys/private.key");

        FileOutputStream outputStream = new FileOutputStream(key);
        outputStream.write(privateKeyBytes);
        outputStream.close();

        List<File> files = new ArrayList<>();
        Collections.addAll(files, jar, key);

        FileOutputStream fos = new FileOutputStream(outputFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        for (File file : files) {
            zos.putNextEntry(new ZipEntry(file.getName()));

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 64];
            int len;
            while ((len = fis.read(buffer)) != -1) {
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


    //toto vrati UserPrincipal objekt, s tym uz normalne pracujes
    //getCurrentPrincipal().getSomeString()
    //getCurrentPrincipal().getUser().getPrivateKey()
    //getUserPrincipal().setAnythingYouWant("anything")
    //etc.
    private UserPrincipal getCurrentPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}