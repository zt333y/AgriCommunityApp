package com.example.agri_app.entity;

public class ReviewVO {
    public Long id;

    // 🌟 核心修复：必须是全小写 username
    public String username;

    public String content;

    // 🌟 核心修复：必须叫 score
    public Integer score;

    public String createTime;
    private String userAvatar;

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}