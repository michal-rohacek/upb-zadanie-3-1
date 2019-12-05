package com.upb.zadanie3.database.user.service;

import com.upb.zadanie3.database.user.domain.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserByUsername(String username);

    void save(User user);

    String getCurrentUsername();

    User getCurrentUser();

}
