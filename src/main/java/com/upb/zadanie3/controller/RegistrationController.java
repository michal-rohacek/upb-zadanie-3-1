package com.upb.zadanie3.controller;

import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.security.Keys;
import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
        return isUserLoggedIn() ? "index" : "registration";
    }

    //TODO nefunguje zobrazovanie error hlasky v pripade zleho loginu
    @GetMapping("sign")
    public String signForm() {
        return isUserLoggedIn() ? "index" : "sign";
    }

    @PostMapping("create-user")
    public String registerUser(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam("password-repeat") String passwordRepeat,
                               RedirectAttributes attributes) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InterruptedException {
        if(!password.equals(passwordRepeat)) {
            attributes.addFlashAttribute("signupMessage", "PASSWORDS DO NOT EQUAL!");
            return "redirect:/registration";
        } else {
            if (cryptoLogic.dictionaryContainsPassword(password)) {
                attributes.addFlashAttribute("signupMessage", "WEAK PASSWORD!");
                return "redirect:/registration";
            }

            String hashed = cryptoLogic.getSaltedHash(password);
            Keys keys = cryptoLogic.generateKeyPairForDB();
            userService.save(new User(username, hashed, keys.publicKey, keys.privateKey));
        }
        attributes.addFlashAttribute("message", "User registered successfully.");
        return "redirect:/sign";
    }

    @PostMapping("sign-user")
    public String signUser(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           RedirectAttributes redirectAttributes) throws InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException {
        System.out.println("ENTERING REG CONTEOLLER POST SIGN USER");
        User user = userService.getUserByUsername(username);
        System.out.println("user got: " + user);
        String message;

        if(user != null && cryptoLogic.comparePasswords(password, user.getPasswordHash())) {
            System.out.println("inside first if");
            return "redirect:/index";
        } else {
            System.out.println("inside else");
            message = "Incorrect password or login!";
            Thread.sleep(5000);
            System.out.println("SPIM LEBO SI ZADAL ZLE HESLOOOOOOOOOO");
            redirectAttributes.addFlashAttribute("message", message);
            return "redirect:/sign";
        }
    }

    private boolean isUserLoggedIn() {
        return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }
}
