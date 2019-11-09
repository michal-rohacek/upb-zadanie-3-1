package com.upb.zadanie3;

import org.springframework.security.crypto.password.PasswordEncoder;

//TEMPORARY - only for testing purposes. Ak chceme skusat prihlasovanie bez hashovania, tj overuje heslo v DB presne ako zadas v login forme.
public class NoPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return charSequence.toString().equals(s);
    }
}
