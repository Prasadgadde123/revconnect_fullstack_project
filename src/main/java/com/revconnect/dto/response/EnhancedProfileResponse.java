package com.revconnect.dto.response;

import java.time.LocalDateTime;

public class EnhancedProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profilePicture;
    private String coverPhoto;
    private String location;
    private String website;
    private String userType;
    private String status;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;
    private Integer profileViews;

    // Enhanced Profile Fields
    private String occupation;
    private String company;
    private String education;
    private String skills;
    private String interests;
    private String phoneNumber;
    private String theme;

    // Social Links
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String youtubeUrl;

    // Privacy Settings (only included for own profile)
    private Boolean showEmail;
    private Boolean showPhone;
    private Boolean showLocation;
    private Boolean showLastActive;
    private Boolean allowMessagesFromAnyone;
    private Boolean allowTagging;

    // Stats
    private Long followersCount;
    private Long followingCount;
    private Long postsCount;

    // Relationship flags
    private Boolean isFollowedByCurrentUser;
    private Boolean isFollowingCurrentUser;
    private Boolean isBlocked;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }

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

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Integer getProfileViews() { return profileViews; }
    public void setProfileViews(Integer profileViews) { this.profileViews = profileViews; }

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

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

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

    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }

    public Long getFollowingCount() { return followingCount; }
    public void setFollowingCount(Long followingCount) { this.followingCount = followingCount; }

    public Long getPostsCount() { return postsCount; }
    public void setPostsCount(Long postsCount) { this.postsCount = postsCount; }

    public Boolean getIsFollowedByCurrentUser() { return isFollowedByCurrentUser; }
    public void setIsFollowedByCurrentUser(Boolean isFollowedByCurrentUser) { this.isFollowedByCurrentUser = isFollowedByCurrentUser; }

    public Boolean getIsFollowingCurrentUser() { return isFollowingCurrentUser; }
    public void setIsFollowingCurrentUser(Boolean isFollowingCurrentUser) { this.isFollowingCurrentUser = isFollowingCurrentUser; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
}