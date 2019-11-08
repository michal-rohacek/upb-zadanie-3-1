package com.upb.zadanie3.controller;

import antlr.collections.impl.IntRange;
import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.security.Keys;
import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private CryptoLogic cryptoLogic;


    public RegistrationController() throws NoSuchAlgorithmException, NoSuchPaddingException {
    }

    @GetMapping("registration")
    public String registrationForm() {
        //TODO not working properly, iba napoly
        if(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            return "index";
        }
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
            if (userService.getUserByUsername(username) == null) {

                if (cryptoLogic.checkPassword(password)) {
                    return "Weak password!";
                }

                String hashed = cryptoLogic.getSaltedHash(password);
                Keys keys = cryptoLogic.generateKeyPairForDB();
                userService.save(new User(username, hashed, keys.publicKey, keys.privateKey));
            } else {
                //redirect to login with msg
            }
        }
        return "redirect:/registration";
    }

    @PostMapping("sign-user")
    public String signUser(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           RedirectAttributes redirectAttributes) throws InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException {

        User user = userService.getUserByUsername(username);
        String message;

        if(user != null && cryptoLogic.comparePasswords(password, user.getPasswordHash())) {
            return "redirect:/index";
        } else {
            message = "Incorrect password or login!";
            Thread.sleep(3000);
        }

        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/sign";
    }

}
