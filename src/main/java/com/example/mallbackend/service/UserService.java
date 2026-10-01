package com.example.mallbackend.service;

import com.example.mallbackend.entity.User;
import com.example.mallbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }

    public User createUser(User user) {
        int rows = userMapper.insertUser(user);
        if(rows == 0) {
            return null;
        }
        return user;
    }

    public boolean updateUser(Long id, User user) {
        int rows = userMapper.updateUser(id, user);
        return rows != 0;
    }

    public boolean deleteUser(Long id) {
        int rows = userMapper.deleteUser(id);
        return rows != 0;
    }
}
