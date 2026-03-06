// src/main/java/com/revconnect/dto/request/ExternalLinkRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ExternalLinkRequest {

    @NotBlank(message = "Label is required")
    @Size(max = 100)
    private String label;

    @NotBlank(message = "URL is required")
    private String url;

    // ENDORSEMENT | PARTNERSHIP | SPONSOR | AFFILIATE | OTHER
    private String linkType = "OTHER";

    private Integer displayOrder = 0;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}