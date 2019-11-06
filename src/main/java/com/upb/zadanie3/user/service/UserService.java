package com.upb.zadanie3.user.service;

import com.upb.zadanie3.user.domain.User;

import java.util.List;

public interface UserService {

    public List<User> getAllUsers();

    public User getUserByLogin(String login);

    public List<String> getAllPublicKeys();

    public void save(User user);

    public User findByLoginAndAndPasswordHash(String login,String passwordHash);
}
