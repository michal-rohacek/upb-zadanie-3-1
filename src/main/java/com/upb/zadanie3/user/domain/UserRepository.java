package com.upb.zadanie3.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByLogin(String login);

    User findByLoginAndAndPasswordHash(String login,String passwordHash);

    List<User> findAll();

}