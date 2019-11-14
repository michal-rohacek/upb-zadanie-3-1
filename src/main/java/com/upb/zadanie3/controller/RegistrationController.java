package com.upb.zadanie3.controller;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.file.domain.IFileRepository;
import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.security.CryptoLogic;
import com.upb.zadanie3.security.Keys;
import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private IFileRepository iFileRepository;

    @Autowired
    private CryptoLogic cryptoLogic;

    public RegistrationController() throws NoSuchAlgorithmException, NoSuchPaddingException {
    }

    @GetMapping("registration")
    public String registrationForm() {
        return isUserLoggedIn() ? "index" : "registration";
    }

    @GetMapping("sign")
    public String signForm(Model model) {
        if (isUserLoggedIn()) {
            model.addAttribute("message", ".");
            model.addAttribute("error", false);
            model.addAttribute("valid", false);
            return "index";
        } else {
            model.addAttribute("message", "Wrong password!");
            model.addAttribute("error", false);
            model.addAttribute("valid", false);
            return "sign";
        }
    }

    @PostMapping("create-user")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam("password-repeat") String passwordRepeat,
                               RedirectAttributes redirectAttributes) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InterruptedException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        if (userService.getUserByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("message", "User with username " + username + " already exists!");
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("valid", false);
            return "redirect:/registration";
        }
        if (!password.equals(passwordRepeat)) {
            redirectAttributes.addFlashAttribute("message", "Passwords do not match!");
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("valid", false);
            return "redirect:/registration";
        } else {
            if (cryptoLogic.dictionaryContainsPassword(password) || cryptoLogic.isPasswordInsecure(password)) {
                redirectAttributes.addFlashAttribute("message", "Password must contains one special character one number and on upper and lower case!");
                redirectAttributes.addFlashAttribute("error", true);
                redirectAttributes.addFlashAttribute("valid", false);
                return "redirect:/registration";
            }

            String hashed = cryptoLogic.getSaltedHash(password);
            Keys keys = cryptoLogic.generateKeyPairForDB();
            userService.save(new User(username, hashed, keys.publicKey, cryptoLogic.encryptPrivateKey(userService, password, hashed, keys.privateKey)));
        }
        redirectAttributes.addFlashAttribute("message", "User " + username + " has been successfully registered");
        redirectAttributes.addFlashAttribute("error", false);
        redirectAttributes.addFlashAttribute("valid", true);
        return "redirect:/sign";
    }

//    @PostMapping("sign-user")
//    public String signUser(@RequestParam("username") String username,
//                           @RequestParam("password") String password,
//                           RedirectAttributes redirectAttributes) throws InvalidKeySpecException, NoSuchAlgorithmException, InterruptedException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
//
//        User user = userService.getUserByUsername(username);
//        if(user != null && cryptoLogic.comparePasswords(password, user.getPasswordHash())) {
//            redirectAttributes.addFlashAttribute("message", "User signed in successfully.");
//            user.setDecryptedPK(cryptoLogic.decryptPrivateKey(userService,password));
//            return "redirect:/index";
//        } else {
//            Thread.sleep(5000);
//            redirectAttributes.addFlashAttribute("message", "Incorrect login or password!");
//            return "redirect:/sign";
//        }
//    }

    @GetMapping("sign-error")
    public String signError(RedirectAttributes attributes) {
        attributes.addFlashAttribute("message", "Login not successful!");
        return "redirect:/sign";
    }

    private boolean isUserLoggedIn() {
        return !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }
}
