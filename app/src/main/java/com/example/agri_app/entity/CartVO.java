package com.example.agri_app.entity;

public class CartVO {
    private Long id;          // 购物车记录的主键
    private Long productId;   // 商品ID
    private String productName; // 商品名称
    private Double price;     // 单价
    private Integer quantity; // 数量
    private String imageUrl;

    // ============ 下面是所有的 Getters 和 Setters ============

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

    // 👉 这里就是你刚才报错缺少的 getPrice 方法！
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    // 👉 这里是获取数量的方法！
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