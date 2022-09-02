package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.model.UserModel;

import java.util.List;

public interface UserService {
    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    List<User> getAllUsers();
}
