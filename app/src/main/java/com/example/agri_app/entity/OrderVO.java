package com.example.agri_app.entity;

public class OrderVO {
    private Long id;
    private String orderNo;     // 订单编号
    private Double totalAmount; // 订单总金额
    private Integer status;     // 订单状态 (0:待发货, 1:已发货, 2:已完成)
    private String createTime;  // 下单时间

    // ============ Getters and Setters ============

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
}