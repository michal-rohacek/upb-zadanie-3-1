package com.upb.zadanie3.controller;

import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.database.user.service.UserService;
import com.upb.zadanie3.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class ReEncryptFileController {

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private IFileRepository fileRepository;

    @PostMapping("/reEncryptToUser")
    public String reEncryptFileToUser(@RequestParam("blabla") String blabla, Model model) throws IOException {

//        TODO dovolil som si pripravit ti controller IVAN

        return "index";
    }
}
