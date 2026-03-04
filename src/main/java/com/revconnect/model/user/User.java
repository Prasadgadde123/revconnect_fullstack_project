// src/main/java/com/revconnect/model/user/User.java
package com.revconnect.model.user;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String bio;
    private String profilePicture;
    private String location;
    private String website;

    private String userType = "PERSONAL";
    private String status = "ACTIVE";
    private Boolean isPrivate = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Security Questions Fields
    private String securityQuestion1;
    private String securityAnswer1;
    private String securityQuestion2;
    private String securityAnswer2;
    private String securityQuestion3;
    private String securityAnswer3;

    // ========== ENHANCED PROFILE FIELDS ==========

    // Professional Information
    private String occupation;
    private String company;
    private String education;
    private String skills;
    private String interests;
    private String phoneNumber;

    // Activity & Stats
    private LocalDateTime lastActive;
    private Integer profileViews = 0;

    // Visual Elements
    private String coverPhoto;
    private String theme = "DEFAULT";

    // Social Links - INCLUDING YOUTUBE
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String youtubeUrl;  // ADDED YOUTUBE URL

    // Privacy Settings (Granular control)
    private Boolean showEmail = false;
    private Boolean showPhone = false;
    private Boolean showLocation = false;
    private Boolean showLastActive = true;
    private Boolean allowMessagesFromAnyone = true;
    private Boolean allowTagging = true;

    // ========== SOFT DELETE FIELDS ==========
    private Boolean deleted = false;
    private String deactivationReason;
    private LocalDateTime deletedAt;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
        this.profileViews = 0;
        this.deleted = false;
    }

    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // ========== BASIC GETTERS AND SETTERS ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ========== SECURITY QUESTIONS GETTERS AND SETTERS ==========

    public String getSecurityQuestion1() { return securityQuestion1; }
    public void setSecurityQuestion1(String securityQuestion1) { this.securityQuestion1 = securityQuestion1; }

    public String getSecurityAnswer1() { return securityAnswer1; }
    public void setSecurityAnswer1(String securityAnswer1) { this.securityAnswer1 = securityAnswer1; }

    public String getSecurityQuestion2() { return securityQuestion2; }
    public void setSecurityQuestion2(String securityQuestion2) { this.securityQuestion2 = securityQuestion2; }

    public String getSecurityAnswer2() { return securityAnswer2; }
    public void setSecurityAnswer2(String securityAnswer2) { this.securityAnswer2 = securityAnswer2; }

    public String getSecurityQuestion3() { return securityQuestion3; }
    public void setSecurityQuestion3(String securityQuestion3) { this.securityQuestion3 = securityQuestion3; }

    public String getSecurityAnswer3() { return securityAnswer3; }
    public void setSecurityAnswer3(String securityAnswer3) { this.securityAnswer3 = securityAnswer3; }

    // ========== ENHANCED PROFILE GETTERS AND SETTERS ==========

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Integer getProfileViews() { return profileViews; }
    public void setProfileViews(Integer profileViews) { this.profileViews = profileViews; }

    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    // ========== SOCIAL LINKS GETTERS AND SETTERS ==========

    public String getFacebookUrl() { return facebookUrl; }
    public void setFacebookUrl(String facebookUrl) { this.facebookUrl = facebookUrl; }

    public String getTwitterUrl() { return twitterUrl; }
    public void setTwitterUrl(String twitterUrl) { this.twitterUrl = twitterUrl; }

    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }

    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }

    public String getGithubUrl() { return githubUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getYoutubeUrl() { return youtubeUrl; }  // ADDED
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }  // ADDED

    // ========== PRIVACY SETTINGS GETTERS AND SETTERS ==========

    public Boolean getShowEmail() { return showEmail; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }

    public Boolean getShowPhone() { return showPhone; }
    public void setShowPhone(Boolean showPhone) { this.showPhone = showPhone; }

    public Boolean getShowLocation() { return showLocation; }
    public void setShowLocation(Boolean showLocation) { this.showLocation = showLocation; }

    public Boolean getShowLastActive() { return showLastActive; }
    public void setShowLastActive(Boolean showLastActive) { this.showLastActive = showLastActive; }

    public Boolean getAllowMessagesFromAnyone() { return allowMessagesFromAnyone; }
    public void setAllowMessagesFromAnyone(Boolean allowMessagesFromAnyone) { this.allowMessagesFromAnyone = allowMessagesFromAnyone; }

    public Boolean getAllowTagging() { return allowTagging; }
    public void setAllowTagging(Boolean allowTagging) { this.allowTagging = allowTagging; }

    // ========== SOFT DELETE GETTERS AND SETTERS ==========

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public String getDeactivationReason() { return deactivationReason; }
    public void setDeactivationReason(String deactivationReason) { this.deactivationReason = deactivationReason; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // ========== HELPER METHODS ==========

    public boolean isActive() {
        return !Boolean.TRUE.equals(this.deleted);
    }

    public boolean isPublic() {
        return !Boolean.TRUE.equals(this.isPrivate);
    }

    public void updateLastActive() {
        this.lastActive = LocalDateTime.now();
    }

    public void incrementProfileViews() {
        if (this.profileViews == null) {
            this.profileViews = 1;
        } else {
            this.profileViews++;
        }
    }

    public void softDelete(String reason) {
        this.deleted = true;
        this.deactivationReason = reason;
        this.deletedAt = LocalDateTime.now();
        this.status = "DELETED";
        this.updatedAt = LocalDateTime.now();

        // Anonymize sensitive data
        this.email = "deleted_" + this.id + "@deleted.com";
        this.username = "user_" + this.id;
    }

    public void reactivate() {
        this.deleted = false;
        this.deactivationReason = null;
        this.deletedAt = null;
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType='" + userType + '\'' +
                ", isPrivate=" + isPrivate +
                ", deleted=" + deleted +
                '}';
    }
}