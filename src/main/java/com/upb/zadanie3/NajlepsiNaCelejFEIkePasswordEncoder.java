package com.upb.zadanie3;

import com.upb.zadanie3.security.CryptoLogic;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class NajlepsiNaCelejFEIkePasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        try {
            CryptoLogic cryptoLogic = new CryptoLogic();
            return cryptoLogic.getSaltedHash(charSequence.toString());
        } catch (NoSuchPaddingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        try {
            CryptoLogic cryptoLogic = new CryptoLogic();
            return cryptoLogic.comparePasswords(charSequence.toString(), s);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
