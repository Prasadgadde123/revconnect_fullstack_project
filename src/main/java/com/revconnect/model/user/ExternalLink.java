// src/main/java/com/revconnect/model/user/ExternalLink.java
package com.revconnect.model.user;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_links")
public class ExternalLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    // ENDORSEMENT | PARTNERSHIP | SPONSOR | AFFILIATE | OTHER
    @Column(name = "link_type")
    private String linkType = "OTHER";

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ExternalLink() {
        this.createdAt = LocalDateTime.now();
    }

    public ExternalLink(BusinessProfile businessProfile, String label, String url, String linkType) {
        this();
        this.businessProfile = businessProfile;
        this.label = label;
        this.url = url;
        this.linkType = linkType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BusinessProfile getBusinessProfile() { return businessProfile; }
    public void setBusinessProfile(BusinessProfile businessProfile) { this.businessProfile = businessProfile; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}