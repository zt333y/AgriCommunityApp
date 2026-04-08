package com.example.agri_app.entity;

public class User {
    private Long id;
    private String username;
    private String password;
    private String phone; // 🌟 新增：手机号字段

    // 保留你原来的构造函数
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ============ 下面是 Getters 和 Setters ============

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 🌟 新增：手机号的 Get 和 Set 方法
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}