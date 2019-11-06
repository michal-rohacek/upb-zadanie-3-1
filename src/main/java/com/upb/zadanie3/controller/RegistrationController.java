package com.upb.zadanie3.controller;

import antlr.collections.impl.IntRange;
import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.security.Keys;
import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.IntStream;

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
                             @RequestParam("password-repeat") String passwordRepeat) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InterruptedException {
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
                         @RequestParam("password") String password,
                           RedirectAttributes redirectAttributes) throws InterruptedException {
        String message;
        User currentUser = userService.findByLoginAndAndPasswordHash(username, password);

        if(currentUser != null){
            return "redirect:/index";
        } else {
            Thread.sleep(3000);
            message = "Pouzivatel s nickom " + username + " nenajdeny!!";
        }
            redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/sign";
    }

}
