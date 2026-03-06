// src/main/java/com/revconnect/dto/request/BusinessProfileRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.Size;

public class BusinessProfileRequest {

    @Size(max = 150)
    private String businessName;
    @Size(max = 100)
    private String category;
    @Size(max = 100)
    private String industry;
    @Size(max = 2000)
    private String detailedBio;

    @Size(max = 255)
    private String contactEmail;
    @Size(max = 30)
    private String contactPhone;
    @Size(max = 500)
    private String websiteUrl;

    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String youtubeUrl;
    private String tiktokUrl;

    // Address (BUSINESS only)
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;

    // Hours (BUSINESS only) — JSON: {"monday":"9:00-17:00","sunday":"Closed"}
    private String businessHours;
    private String timezone;

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
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}