// src/main/java/com/revconnect/dto/request/ProductServiceRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductServiceRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    // PRODUCT | SERVICE
    private String itemType = "PRODUCT";

    private BigDecimal price;
    private String currency = "USD";
    private String imageUrl;
    private String purchaseUrl;

    // ACTIVE | INACTIVE | OUT_OF_STOCK
    private String status = "ACTIVE";

    private Integer displayOrder = 0;

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
}