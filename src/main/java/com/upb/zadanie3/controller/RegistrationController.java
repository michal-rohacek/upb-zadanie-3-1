package com.upb.zadanie3.controller;

import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.security.Keys;
import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;


    private CryptoLogic cryptoLogic = new CryptoLogic();


    public RegistrationController() throws NoSuchAlgorithmException, NoSuchPaddingException {
    }

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
                             @RequestParam("password-repeat") String passwordRepeat) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if(!password.equals(passwordRepeat)) {
            return "redirect:/registration";
        } else {
            if(userService.getUserByLogin(username) == null) {
                Keys keys = cryptoLogic.generateKeyPairForDB();
                userService.save(new User(username, password, keys.publicKey,keys.privateKey));
            } else {
                //redirect to login with msg
            }
        }
        return "redirect:/registration";
    }

    @PostMapping("sign-user")
    public String signUser(@RequestParam("username") String username,
                         @RequestParam("password") String password) {
        return "redirect:/sign";
    }

}
