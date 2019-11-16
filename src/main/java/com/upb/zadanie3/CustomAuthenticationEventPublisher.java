package com.upb.zadanie3;

import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.domain.UserRepository;
import com.upb.zadanie3.security.CryptoLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CustomAuthenticationEventPublisher implements AuthenticationEventPublisher {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        UserPrincipal loggedUser = (UserPrincipal) authentication.getPrincipal();
        try
        {
            CryptoLogic cryptoLogic = new CryptoLogic();
            loggedUser.getUser().setDecryptedPK(cryptoLogic.decryptPrivateKey(loggedUser.getUser(), authentication.getCredentials().toString()));
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        System.out.println("TUTO EVENTY V PRIPADE NEPODARENEHO LOGINU - toto asi nepotrebujeme");
    }
}
