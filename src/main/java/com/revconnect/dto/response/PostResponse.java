// src/main/java/com/revconnect/dto/response/PostResponse.java
package com.revconnect.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    private Long id;
    private String content;
    private String hashtags;
    private List<String> hashtagList;
    private String postType;

    // Author info (denormalized for display)
    private Long authorId;
    private String authorUsername;
    private String authorFullName;
    private String authorProfilePicture;

    // Repost info
    private Long originalPostId;
    private String originalAuthorUsername;
    private String originalAuthorFullName;
    private String originalContent;
    private String repostComment;

    // Stats
    private Integer likesCount;
    private Integer commentsCount;
    private Integer repostsCount;

    // ─── FEATURE 1: Tagged Products ─────────────────────────
    private List<TaggedProductDto> taggedProducts;

    // ─── FEATURE 2: Schedule ────────────────────────────────
    private LocalDateTime scheduledAt;
    private boolean isScheduled; // true when status = 'SCHEDULED'

    // ─── FEATURE 3: Pin ─────────────────────────────────────
    private boolean isPinned;
    private LocalDateTime pinnedAt;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Viewer context
    private boolean isOwnPost;
    private boolean likedByMe;

    // ─── Nested DTO for tagged products ─────────────────────
    public static class TaggedProductDto {
        private Long id;
        private String name;
        private String itemType;   // PRODUCT | SERVICE
        private String price;      // formatted, e.g. "$9.99"
        private String imageUrl;
        private String purchaseUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getPurchaseUrl() { return purchaseUrl; }
        public void setPurchaseUrl(String purchaseUrl) { this.purchaseUrl = purchaseUrl; }
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public List<String> getHashtagList() { return hashtagList; }
    public void setHashtagList(List<String> hashtagList) { this.hashtagList = hashtagList; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getAuthorFullName() { return authorFullName; }
    public void setAuthorFullName(String authorFullName) { this.authorFullName = authorFullName; }

    public String getAuthorProfilePicture() { return authorProfilePicture; }
    public void setAuthorProfilePicture(String authorProfilePicture) { this.authorProfilePicture = authorProfilePicture; }

    public Long getOriginalPostId() { return originalPostId; }
    public void setOriginalPostId(Long originalPostId) { this.originalPostId = originalPostId; }

    public String getOriginalAuthorUsername() { return originalAuthorUsername; }
    public void setOriginalAuthorUsername(String originalAuthorUsername) { this.originalAuthorUsername = originalAuthorUsername; }

    public String getOriginalAuthorFullName() { return originalAuthorFullName; }
    public void setOriginalAuthorFullName(String originalAuthorFullName) { this.originalAuthorFullName = originalAuthorFullName; }

    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }

    public String getRepostComment() { return repostComment; }
    public void setRepostComment(String repostComment) { this.repostComment = repostComment; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Integer getRepostsCount() { return repostsCount; }
    public void setRepostsCount(Integer repostsCount) { this.repostsCount = repostsCount; }

    public List<TaggedProductDto> getTaggedProducts() { return taggedProducts; }
    public void setTaggedProducts(List<TaggedProductDto> taggedProducts) { this.taggedProducts = taggedProducts; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isScheduled() { return isScheduled; }
    public void setScheduled(boolean scheduled) { isScheduled = scheduled; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(LocalDateTime pinnedAt) { this.pinnedAt = pinnedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOwnPost() { return isOwnPost; }
    public void setOwnPost(boolean ownPost) { isOwnPost = ownPost; }

    public boolean isLikedByMe() { return likedByMe; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
}