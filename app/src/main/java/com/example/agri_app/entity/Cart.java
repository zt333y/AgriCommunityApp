package com.example.agri_app.entity;

public class Cart {
    public Long userId;
    public Long productId;
    public Integer quantity;

    public Cart(Long userId, Long productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}