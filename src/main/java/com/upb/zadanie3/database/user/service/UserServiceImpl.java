package com.upb.zadanie3.database.user.service;

import com.upb.zadanie3.database.user.domain.User;
import com.upb.zadanie3.database.user.domain.UserPrincipal;
import com.upb.zadanie3.database.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    public String getCurrentUsername() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUsername();
    }

    public User getCurrentUser() {
        return getUserByUsername(getCurrentUsername());
    }

}
