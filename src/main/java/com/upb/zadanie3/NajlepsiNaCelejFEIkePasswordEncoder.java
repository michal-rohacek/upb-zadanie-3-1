package com.upb.zadanie3;

import com.upb.zadanie3.security.CryptoLogic;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class NajlepsiNaCelejFEIkePasswordEncoder implements PasswordEncoder {

    private CryptoLogic cryptoLogic;

    @Override
    public String encode(CharSequence charSequence) {
        //TODO zistit ci je toto vobec treba, resp prihlasovanie funguje aj bez tohto
        //ten kod co je nizsie je asi blbost
        return charSequence.toString();
        //        try {
        //            this.cryptoLogic = new CryptoLogic();
        //            System.out.println("ENCODE");
        //            System.out.println("chats:" + charSequence.toString());
        //            String result = cryptoLogic.getSaltedHash(charSequence.toString());
        //            System.out.println("result: " + result);
        //            return result;
        //        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        //            throw new RuntimeException(e);
        //        }
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        try {
            this.cryptoLogic = new CryptoLogic();
            return cryptoLogic.comparePasswords(charSequence.toString(), s);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
