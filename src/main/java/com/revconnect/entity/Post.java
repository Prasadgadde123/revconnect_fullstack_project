package com.revconnect.entity;

import com.revconnect.enums.PostType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostType postType = PostType.REGULAR;

    private String hashtags; // comma-separated
    private String imageUrl;
    private String ctaButtonText;
    private String ctaButtonUrl;

    @Builder.Default
    private boolean pinned = false;

    @Builder.Default
    private boolean deleted = false;

    // For reposts
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime scheduledAt;

    // Likes
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Builder.Default
    private Set<User> likes = new HashSet<>();

    // Comments
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // Reposts (posts that reposted this post)
    @OneToMany(mappedBy = "originalPost", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Post> reposts = new ArrayList<>();

    // Tagged products/services (for creator/business posts)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "post_tagged_products", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "product_name")
    @Builder.Default
    private List<String> taggedProducts = new ArrayList<>();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public int getLikeCount() { return likes.size(); }
    public int getCommentCount() { return (int) comments.stream().filter(c -> !c.isDeleted()).count(); }
    public int getRepostCount() { return reposts != null ? (int) reposts.stream().filter(r -> !r.isDeleted()).count() : 0; }

    public boolean isLikedBy(User user) {
        return likes.contains(user);
    }

    public List<String> getHashtagList() {
        if (hashtags == null || hashtags.isBlank()) return new ArrayList<>();
        return List.of(hashtags.split(","));
    }
}
