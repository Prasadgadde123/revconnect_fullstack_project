// src/main/java/com/revconnect/dto/request/CreatePostRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class CreatePostRequest {

    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 2000, message = "Post content cannot exceed 2000 characters")
    private String content;

    private String hashtags; // optional comma-separated, e.g. "java,spring"

    // ─── FEATURE 1: Tag Products/Services ───────────────────
    /** IDs from products_services table belonging to the creator's business profile. */
    private List<Long> taggedProductIds;

    // ─── FEATURE 2: Schedule Post ────────────────────────────
    /** If set, post is saved with status='SCHEDULED' and published at this time. */
    private LocalDateTime scheduledAt;

    // ─── FEATURE 3: Pin Post ─────────────────────────────────
    /** If true, post is pinned immediately after creation. */
    private Boolean pinPost = false;

    // ─── Getters & Setters ───────────────────────────────────
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public List<Long> getTaggedProductIds() { return taggedProductIds; }
    public void setTaggedProductIds(List<Long> taggedProductIds) { this.taggedProductIds = taggedProductIds; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Boolean getPinPost() { return pinPost; }
    public void setPinPost(Boolean pinPost) { this.pinPost = pinPost; }
}