package com.example.agri_app.entity;

import java.io.Serializable;

public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    // 🌟 核心修改：添加 role 字段，对应数据库中的角色(0居民, 1农户, 2团长, 3管理)
    private Integer role;
    private Long communityId;
    private String address;

    // 必须要有一个无参构造函数
    public User() {}

    // 如果你之前有这个构造函数，请保留
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ---------------- Getter 和 Setter 方法 ----------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 🌟 核心修改：添加 getRole 方法，解决 MainActivity 的报错
    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}