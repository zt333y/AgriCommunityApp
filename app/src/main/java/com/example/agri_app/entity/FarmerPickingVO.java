package com.example.agri_app.entity;

/**
 * 农户采摘汇总视图对象
 */
public class FarmerPickingVO {
    private Long productId;
    private String productName;
    private Integer totalQuantity;
    private String unit;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}