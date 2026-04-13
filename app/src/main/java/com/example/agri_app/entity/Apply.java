package com.example.agri_app.entity;

/**
 * 资质入驻申请实体类
 */
public class Apply {
    private Long id;
    private Long userId;
    private Integer applyRole; // 1:农户, 2:团长
    private String realName;
    private String idCard;     // 🌟 实体类属性统一用驼峰命名
    private String address;
    private Integer status;    // 0:待审批, 1:已通过, 2:已驳回
    private String reason;

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getApplyRole() { return applyRole; }
    public void setApplyRole(Integer applyRole) { this.applyRole = applyRole; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    // 🌟 修复：这里的 return 变量名必须和上面的属性名完全一致
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}