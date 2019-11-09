package com.upb.zadanie3.user.service;

import com.upb.zadanie3.user.domain.User;
import com.upb.zadanie3.user.domain.UserPrincipal;
import com.upb.zadanie3.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User foundUser = userRepository.findByUsername(username);
        if (foundUser == null) {
            throw new UsernameNotFoundException("No user found with username: " + username);
        }
        return new UserPrincipal(foundUser);
    }



}
