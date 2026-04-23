package com.example.agri_app.entity;

import com.google.gson.annotations.SerializedName; // 🌟 新增导入这个神奇的包

public class CartVO {

    // 🌟 核心修复：强行把后端的 "cartId" 映射到前端的 "id" 上！
    @SerializedName("cartId")
    private Long id;          // 购物车记录的主键

    private Long productId;   // 商品ID
    private String productName; // 商品名称
    private Double price;     // 单价
    private Integer quantity; // 数量
    private String imageUrl;

    // ============ 下面是原封不动的 Getters 和 Setters ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}