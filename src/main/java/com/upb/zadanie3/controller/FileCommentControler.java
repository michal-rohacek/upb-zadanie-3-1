
package com.upb.zadanie3.controller;

import com.upb.zadanie3.FileDto;
import com.upb.zadanie3.database.comment.domain.Comment;
import com.upb.zadanie3.database.comment.domain.CommentRepository;
import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.service.UserService;
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

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/search")
    public String getSearchPage() throws IOException {
        return "search";
    }

    @GetMapping("/seeAllFiles")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("fileDtos", getDTOsFromFilenames(getFilenamesFilteredBy(x -> true)));
        model.addAttribute("myFiles", false);

        return "viewFiles";
    }

    @GetMapping("/seeMyFiles")
    public String listCurrentUserFiles(Model model) throws IOException {
        List<EncryptedFile> userFiles = fileRepository.getAllByRecipientUser(getCurrentUser());
        List<String> filenames = getFilenamesFilteredBy(filename -> userFiles.stream().map(EncryptedFile::getFileName).anyMatch(filename::equals));

        model.addAttribute("fileDtos", getDTOsFromFilenames(filenames));
        model.addAttribute("myFiles", true);

        return "viewFiles";
    }

    @PostMapping("/seeFilteredFiles")
    public String listFilteredFiles(@RequestParam("search") String search, @RequestParam("myFiles") boolean myFiles, Model model) throws IOException {
        List<EncryptedFile> foundFiles = findFilesContaining(search, myFiles ? fileRepository.getAllByRecipientUser(getCurrentUser()) : fileRepository.findAll());
        List<String> filenames = getFilenamesFilteredBy(filename -> foundFiles.stream().map(EncryptedFile::getFileName).anyMatch(filename::equals));

        model.addAttribute("fileDtos", getDTOsFromFilenames(filenames));

        return "viewFiles";
    }


    private List<String> getFilenamesFilteredBy(Predicate<String> filePredicate) {
        return fileRepository.findAll().stream()
                .map(EncryptedFile::getFileName)
                .filter(filePredicate)
                .collect(Collectors.toList());
    }

    private List<FileDto> getDTOsFromFilenames(List<String> filenames) {
        List<FileDto> dtos = new ArrayList<>();
        for(String filename : filenames) {
            EncryptedFile file = fileRepository.findEncryptedFilesByFileName(filename);
            FileDto dto = new FileDto();
            dto.id = file.getId();
            dto.fileLink = getURI(filename);
            dto.comments.addAll(file.getComments());
            dtos.add(dto);
        }
        return dtos;
    }

    @GetMapping("/seeAll/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFileComment(@PathVariable String filename) {
        Resource file = storageService.loadFiles(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/addComment")
    public String addComment(@RequestParam("fileId") Integer fileId, @RequestParam("comment") String comment, @RequestParam("myFiles") boolean myFiles) {
        String redirectEndpoint = myFiles ? "./seeMyFiles" : "./seeAllFiles";
        if (comment.isEmpty())  return "redirect:" + redirectEndpoint;
        EncryptedFile file = fileRepository.findEncryptedFileById(fileId);
        Comment com = new Comment();
        com.setComment(comment);
        com.setEncryptedFile(file);
        com.setUserCreator(getCurrentUser());
        file.getComments().add(com);
        fileRepository.save(file);
        commentRepository.save(com);
        return "redirect:" + redirectEndpoint;
    }

    private String getCurrentUsername() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUsername();
    }

    private User getCurrentUser() {
        return userService.getUserByUsername(getCurrentUsername());
    }

    private String getURI(String filename) {
        return MvcUriComponentsBuilder.fromMethodName(FileCommentControler.class,"serveFileComment", filename).build().toString();
    }

    private List<EncryptedFile> findFilesContaining(String search, List<EncryptedFile> files) {
        List<EncryptedFile> result = new ArrayList<>();
        for(EncryptedFile file: files) {
            for(Comment comment : file.getComments()) {
                if(comment.getComment() != null && comment.getComment().contains(search))
                    result.add(file);
            }
            if (file.getFileName().contains(search)) {
                result.add(file);
            }
         }
        return result;
    }
}