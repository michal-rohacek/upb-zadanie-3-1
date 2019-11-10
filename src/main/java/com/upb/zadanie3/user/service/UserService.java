package com.upb.zadanie3.user.service;

import com.upb.zadanie3.user.domain.User;

import java.util.List;

public interface UserService {

    public List<User> getAllUsers();

    public User getUserByUsername(String username);

    public void save(User user);

}
