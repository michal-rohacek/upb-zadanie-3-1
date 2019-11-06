package com.upb.zadanie3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    @GetMapping("registration")
    public String registrationForm() {
        return "registration";
    }

    @GetMapping("sign")
    public String signForm() {
        return "sign";
    }

    @PostMapping("create-user")
    public String registerUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("password-repeat") String passwordRepeat) {
        if(!password.equals(passwordRepeat)) {
            return "redirect:/registration";
        }
        return "redirect:/registration";
    }

    @PostMapping("sign-user")
    public String signUser(@RequestParam("username") String username,
                         @RequestParam("password") String password) {
        return "redirect:/sign";
    }

}
