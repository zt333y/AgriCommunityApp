package com.example.agri_app.entity; // 注意包名和你自己的一致

public class Product {
    private Long id;
    private Long farmerId;
    private String name;
    private String category; // 🌟 新增：分类
    private Double price;
    private Integer stock;   // 🌟 新增：库存
    private String unit;     // 🌟 新增：单位
    private String imageUrl;
    private String description;
    private Integer status;

    // 无参构造函数
    public Product() {}

    // ---------------- Getter 和 Setter 方法 ----------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // 🌟 新增的方法
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    // 🌟 新增的方法
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    // 🌟 新增的方法
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

}