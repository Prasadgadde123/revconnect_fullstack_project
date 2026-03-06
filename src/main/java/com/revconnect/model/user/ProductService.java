// src/main/java/com/revconnect/model/user/ProductService.java
package com.revconnect.model.user;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products_services")
public class ProductService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // PRODUCT | SERVICE
    @Column(name = "item_type")
    private String itemType = "PRODUCT";

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency")
    private String currency = "USD";

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "purchase_url", columnDefinition = "TEXT")
    private String purchaseUrl;

    // ACTIVE | INACTIVE | OUT_OF_STOCK
    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProductService() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ProductService(BusinessProfile businessProfile, String name, String itemType) {
        this();
        this.businessProfile = businessProfile;
        this.name = name;
        this.itemType = itemType;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessProfile getBusinessProfile() { return businessProfile; }
    public void setBusinessProfile(BusinessProfile businessProfile) { this.businessProfile = businessProfile; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getPurchaseUrl() { return purchaseUrl; }
    public void setPurchaseUrl(String purchaseUrl) { this.purchaseUrl = purchaseUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}