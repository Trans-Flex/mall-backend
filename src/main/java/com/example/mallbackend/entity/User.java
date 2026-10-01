package com.example.mallbackend.entity;

public class User {
    private Long id;
    private String username;
    private String password;
    private String phone;

    public User(Long id, String userName, String passWord, String phone) {
        this.id = id;
        this.username = userName;
        this.password = passWord;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public void setPassword(String passWord) {
        this.password = passWord;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
