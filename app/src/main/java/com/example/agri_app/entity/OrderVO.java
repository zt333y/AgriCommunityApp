package com.example.agri_app.entity;

import java.util.List;

public class OrderVO {
    private Long id;
    private String orderNo;
    private Double totalAmount;
    private Integer status;
    private String createTime;
    private Long productId;

    // 🌟 核心新增：专门用来存放当前订单下的商品明细列表
    private List<OrderItem> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    // 🌟 提供给 Adapter 调用的方法，解决报错
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}