package com.example.agri_app.entity;

public class User {
    private Long id;          // 核心字段：必须要有 id，用于存入本地
    private String username;
    private String password;

    // 如果你的后端 User 表还有真实姓名、电话等，也可以写在这里
    // private String realName;
    // private String phone;

    // 无参构造函数（Retrofit 解析 JSON 时必须要用到）
    public User() {
    }

    // 用于咱们在 MainActivity 发起登录请求时用的构造函数
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ========== 下面是所有字段的 Getters 和 Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}