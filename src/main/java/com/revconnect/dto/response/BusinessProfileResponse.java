// src/main/java/com/revconnect/dto/response/BusinessProfileResponse.java
package com.revconnect.dto.response;

import com.revconnect.model.user.BusinessProfile;
import com.revconnect.model.user.ExternalLink;
import com.revconnect.model.user.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessProfileResponse {

    private Long id;
    private Long userId;
    private String username;
    private String businessName;
    private String category;
    private String industry;
    private String detailedBio;
    private String contactEmail;
    private String contactPhone;
    private String websiteUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private String tiktokUrl;
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;
    private String fullAddress;
    private String businessHours;
    private String timezone;
    private List<ExternalLinkDto> externalLinks;
    private List<ProductServiceDto> productsServices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BusinessProfileResponse from(BusinessProfile bp) {
        BusinessProfileResponse r = new BusinessProfileResponse();
        r.setId(bp.getId());
        r.setUserId(bp.getUser().getId());
        r.setUsername(bp.getUser().getUsername());
        r.setBusinessName(bp.getBusinessName());
        r.setCategory(bp.getCategory());
        r.setIndustry(bp.getIndustry());
        r.setDetailedBio(bp.getDetailedBio());
        r.setContactEmail(bp.getContactEmail());
        r.setContactPhone(bp.getContactPhone());
        r.setWebsiteUrl(bp.getWebsiteUrl());
        r.setFacebookUrl(bp.getFacebookUrl());
        r.setTwitterUrl(bp.getTwitterUrl());
        r.setInstagramUrl(bp.getInstagramUrl());
        r.setLinkedinUrl(bp.getLinkedinUrl());
        r.setYoutubeUrl(bp.getYoutubeUrl());
        r.setTiktokUrl(bp.getTiktokUrl());
        r.setStreetAddress(bp.getStreetAddress());
        r.setCity(bp.getCity());
        r.setStateProvince(bp.getStateProvince());
        r.setPostalCode(bp.getPostalCode());
        r.setCountry(bp.getCountry());
        r.setFullAddress(bp.getFullAddress());
        r.setBusinessHours(bp.getBusinessHours());
        r.setTimezone(bp.getTimezone());
        r.setCreatedAt(bp.getCreatedAt());
        r.setUpdatedAt(bp.getUpdatedAt());
        r.setExternalLinks(bp.getExternalLinks().stream()
                .map(ExternalLinkDto::from).collect(Collectors.toList()));
        r.setProductsServices(bp.getProductsServices().stream()
                .filter(ps -> "ACTIVE".equals(ps.getStatus()))
                .map(ProductServiceDto::from).collect(Collectors.toList()));
        return r;
    }

    // ── Inner DTOs ──────────────────────────────────────────

    public static class ExternalLinkDto {
        private Long id;
        private String label;
        private String url;
        private String linkType;
        private Integer displayOrder;

        public static ExternalLinkDto from(ExternalLink el) {
            ExternalLinkDto d = new ExternalLinkDto();
            d.setId(el.getId());
            d.setLabel(el.getLabel());
            d.setUrl(el.getUrl());
            d.setLinkType(el.getLinkType());
            d.setDisplayOrder(el.getDisplayOrder());
            return d;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getLinkType() { return linkType; }
        public void setLinkType(String linkType) { this.linkType = linkType; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class ProductServiceDto {
        private Long id;
        private String name;
        private String description;
        private String itemType;
        private BigDecimal price;
        private String currency;
        private String imageUrl;
        private String purchaseUrl;
        private String status;
        private Integer displayOrder;

        public static ProductServiceDto from(ProductService ps) {
            ProductServiceDto d = new ProductServiceDto();
            d.setId(ps.getId());
            d.setName(ps.getName());
            d.setDescription(ps.getDescription());
            d.setItemType(ps.getItemType());
            d.setPrice(ps.getPrice());
            d.setCurrency(ps.getCurrency());
            d.setImageUrl(ps.getImageUrl());
            d.setPurchaseUrl(ps.getPurchaseUrl());
            d.setStatus(ps.getStatus());
            d.setDisplayOrder(ps.getDisplayOrder());
            return d;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
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

    // ── Getters & Setters ───────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getDetailedBio() { return detailedBio; }
    public void setDetailedBio(String detailedBio) { this.detailedBio = detailedBio; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }
    public String getTwitterUrl() { return twitterUrl; }
    public void setTwitterUrl(String twitterUrl) { this.twitterUrl = twitterUrl; }
    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public String getTiktokUrl() { return tiktokUrl; }
    public void setTiktokUrl(String tiktokUrl) { this.tiktokUrl = tiktokUrl; }
    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStateProvince() { return stateProvince; }
    public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public List<ExternalLinkDto> getExternalLinks() { return externalLinks; }
    public void setExternalLinks(List<ExternalLinkDto> externalLinks) { this.externalLinks = externalLinks; }
    public List<ProductServiceDto> getProductsServices() { return productsServices; }
    public void setProductsServices(List<ProductServiceDto> productsServices) { this.productsServices = productsServices; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}