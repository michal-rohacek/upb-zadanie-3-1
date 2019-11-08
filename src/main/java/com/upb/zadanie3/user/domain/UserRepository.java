package com.upb.zadanie3.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    User findByUsernameAndAndPasswordHash(String username, String passwordHash);

    List<User> findAll();

}