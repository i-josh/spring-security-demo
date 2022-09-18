package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/fetch")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/fetch/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userService.getUser(id).orElseThrow(IllegalStateException::new);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "deleted";
    }
}
