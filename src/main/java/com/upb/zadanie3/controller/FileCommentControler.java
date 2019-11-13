
package com.upb.zadanie3.controller;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.service.UserService;
import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.storage.LocationConfig;
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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class FileCommentControler {

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;


    @Autowired
    private IFileRepository fileRepository;

    @GetMapping("/seeAll")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll(LocationConfig.filesLocation).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileCommentControler.class,
                        "serveFileComment", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));
        List<String> names = new ArrayList<String>();
        for (User user : userService.getAllUsers()) {
            names.add(user.getUsername());
        }
        return "seeAll";
    }

    @GetMapping("/seeAll/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFileComment(@PathVariable String filename) {
        Resource file = storageService.loadFiles(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }





//    @GetMapping("/error")
//    @ResponseBody
//    public String errorController() {
//        return "error";
//    }
}