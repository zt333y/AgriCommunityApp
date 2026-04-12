package com.example.agri_app.entity;

public class Review {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long productId;
    private String content;
    private Integer score;

    // 按 Alt + Insert (或右键 Generate) 生成 Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}