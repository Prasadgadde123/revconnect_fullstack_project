// src/main/java/com/revconnect/model/post/Post.java
package com.revconnect.model.post;

import com.revconnect.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Relationship ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ─── Content ────────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "hashtags")
    private String hashtags;          // stored as comma-separated, e.g. "spring,java,revconnect"

    // ─── Type ───────────────────────────────────────────────
    @Column(name = "post_type")
    private String postType = "TEXT"; // TEXT | REPOST

    // ─── Repost fields ──────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;        // null if not a repost

    @Column(name = "repost_comment", columnDefinition = "TEXT")
    private String repostComment;     // optional comment when reposting

    // ─── Status ─────────────────────────────────────────────
    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE | DELETED

    // ─── Stats (denormalized counters, updated by service) ──
    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    @Column(name = "reposts_count")
    private Integer repostsCount = 0;

    // ─── Timestamps ─────────────────────────────────────────
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Constructors ────────────────────────────────────────
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.likesCount = 0;
        this.commentsCount = 0;
        this.repostsCount = 0;
        this.status = "ACTIVE";
        this.postType = "TEXT";
    }

    public Post(User user, String content) {
        this();
        this.user = user;
        this.content = content;
    }

    // ─── Lifecycle ───────────────────────────────────────────
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Helper methods ──────────────────────────────────────
    public boolean isOwnedBy(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    public boolean isActive() {
        return "ACTIVE".equals(this.status);
    }

    public boolean isRepost() {
        return "REPOST".equals(this.postType);
    }

    public List<String> getHashtagList() {
        if (hashtags == null || hashtags.isBlank()) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (String tag : hashtags.split(",")) {
            String t = tag.trim();
            if (!t.isEmpty()) list.add(t.startsWith("#") ? t : "#" + t);
        }
        return list;
    }

    public void softDelete() {
        this.status = "DELETED";
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ───────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public Post getOriginalPost() { return originalPost; }
    public void setOriginalPost(Post originalPost) { this.originalPost = originalPost; }

    public String getRepostComment() { return repostComment; }
    public void setRepostComment(String repostComment) { this.repostComment = repostComment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Integer getRepostsCount() { return repostsCount; }
    public void setRepostsCount(Integer repostsCount) { this.repostsCount = repostsCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Post{id=" + id + ", user=" + (user != null ? user.getUsername() : "null")
                + ", postType='" + postType + "', status='" + status + "'}";
    }
}