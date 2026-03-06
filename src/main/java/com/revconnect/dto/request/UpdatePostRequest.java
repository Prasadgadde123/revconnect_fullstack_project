// src/main/java/com/revconnect/dto/request/UpdatePostRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class UpdatePostRequest {

    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 2000, message = "Post content cannot exceed 2000 characters")
    private String content;

    private String hashtags;

    // ─── FEATURE 1: Tag Products/Services ───────────────────
    private List<Long> taggedProductIds;

    // ─── FEATURE 2: Schedule Post ────────────────────────────
    /** Set to a future datetime to reschedule; set to null to publish immediately. */
    private LocalDateTime scheduledAt;

    // ─── Getters & Setters ───────────────────────────────────
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public List<Long> getTaggedProductIds() { return taggedProductIds; }
    public void setTaggedProductIds(List<Long> taggedProductIds) { this.taggedProductIds = taggedProductIds; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}