package com.example.agri_app.entity;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String unit;
    private String imageUrl;
    private Integer status;
    private String category;
    private Long farmerId;

    // 🌟 本次核心新增的三个字段，用于接收后端传来的销量、评分和产地
    private Integer sales;
    private Double rating;
    private String farmerAddress;

    // ======= 下面是所有字段的 Getter 和 Setter =======

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    // 🌟 新增字段的 Getter 和 Setter
    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getFarmerAddress() { return farmerAddress; }
    public void setFarmerAddress(String farmerAddress) { this.farmerAddress = farmerAddress; }
}