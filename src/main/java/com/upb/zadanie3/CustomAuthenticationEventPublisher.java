package com.upb.zadanie3;

import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEventPublisher implements AuthenticationEventPublisher {

    @Autowired
    private UserRepository userRepository;

    // Eventy po podarenom logine
    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {

        // objekt reprezentujuci prave prihlaseneho usera
        UserPrincipal currentLogged = (UserPrincipal) authentication.getPrincipal();

        // v UserPrincipal someString mozes zmenit na privateKey, alebo co len chces, nezabudni na gettre a settre
        // treba tu setnut tu property, a potom ju mozes pouzivat hocikde
        // vo FileUploadControlleri je uplne dole nova helper metoda, ktora vrati instanciu UserPrincipal
        currentLogged.setSomeString("TOTO JE ODSIFROVANY PRIVATNY KLUC V SESSION KTORY SA ULOZIL PO PRIHLASENI");

    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        System.out.println("TUTO EVENTY V PRIPADE NEPODARENEHO LOGINU - toto asi nepotrebujeme");
    }
}
