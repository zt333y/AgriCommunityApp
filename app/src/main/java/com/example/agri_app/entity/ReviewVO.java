package com.example.agri_app.entity;

public class ReviewVO {
    public Long id;
    public String userName; // 评价人名字
    public String content;  // 评价内容

    // 🌟 核心修复：将原来的 rating 改为 score，这样就能成功接收后端传来的真实星级了！
    public Integer score;

    public String createTime; // 评价时间
}