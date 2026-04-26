package com.example.agri_app.entity;

public class OrderItem {
    public Long id;
    public Long orderId;
    public Long productId;
    public String productName;
    public Integer quantity; // 🌟 必须要有购买数量

    // 🌟 提供给 Adapter 调用的方法，解决报错
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String imageUrl; // 🌟 新增

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}