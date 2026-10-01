package com.example.mallbackend.controller;

import com.example.mallbackend.entity.User;
import com.example.mallbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users") public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users") public User insertUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}") public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/users/{id}") public User updateUser(@PathVariable Long id, @RequestBody User user) {
        boolean ok = userService.updateUser(id, user);
        if(!ok) {
            return null;
        }
        return userService.getUserById(id);
    }

    @DeleteMapping("/users/{id}") public String deleteUser(@PathVariable Long id) {
        boolean ok = userService.deleteUser(id);
        if(!ok) {
            return "用户不存在";
        }
        return "删除成功";
    }
}
