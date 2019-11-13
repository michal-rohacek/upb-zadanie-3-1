
package com.upb.zadanie3.controller;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.service.UserService;
import com.upb.zadanie3.storage.LocationConfig;
import com.upb.zadanie3.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Controller
public class FileCommentControler {

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private IFileRepository fileRepository;

    @GetMapping("/seeAllFiles")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files", getFilesFilteredBy(x -> true));
        return "viewFiles";
    }

    @GetMapping("/seeMyFiles")
    public String listCurrentUserFiles(Model model) throws IOException {
        List<EncryptedFile> userFiles = fileRepository.getAllByRecipientUser(getCurrentUser());

        model.addAttribute("files",
                getFilesFilteredBy(filePath -> userFiles.stream()
                        .map(EncryptedFile::getFileName)
                        .anyMatch(filePath.toString()::equals)));

        return "viewFiles";
    }

    private List<String> getFilesFilteredBy(Predicate<Path> filePredicate) {
        return storageService.loadAll(LocationConfig.filesLocation)
                .filter(filePredicate)
                .map(path -> MvcUriComponentsBuilder.fromMethodName(FileCommentControler.class,
                        "serveFileComment", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/seeAll/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFileComment(@PathVariable String filename) {
        Resource file = storageService.loadFiles(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }


    private String getCurrentUsername() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUsername();
    }

    private User getCurrentUser() {
        return userService.getUserByUsername(getCurrentUsername());
    }



//    @GetMapping("/error")
//    @ResponseBody
//    public String errorController() {
//        return "error";
//    }
}