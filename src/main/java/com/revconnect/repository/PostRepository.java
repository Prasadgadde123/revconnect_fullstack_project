// src/main/java/com/revconnect/repository/PostRepository.java
package com.revconnect.repository;

import com.revconnect.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ─── Own posts (for profile page) ────────────────────────────────────────

    /** All active posts by a user, newest first */
    Page<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    List<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    /** Count active posts for a user */
    long countByUserIdAndStatus(Long userId, String status);

    // ─── Feed posts ──────────────────────────────────────────────────────────

    /**
     * Feed: posts from users the current user follows OR their own posts.
     * followedUserIds should include the current user's own id so their posts
     * also appear in their feed.
     */
    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.status = 'ACTIVE' " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findFeedPosts(@Param("userIds") List<Long> userIds, Pageable pageable);

    // ─── Single post ─────────────────────────────────────────────────────────

    Optional<Post> findByIdAndStatus(Long id, String status);

    // ─── Hashtag search ──────────────────────────────────────────────────────

    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.hashtags LIKE %:hashtag% " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // ─── Trending (most liked active posts) ─────────────────────────────────

    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' ORDER BY p.likesCount DESC, p.createdAt DESC")
    Page<Post> findTrendingPosts(Pageable pageable);

    // ─── Reposts of an original post ─────────────────────────────────────────

    List<Post> findByOriginalPostIdAndStatus(Long originalPostId, String status);

    long countByOriginalPostIdAndStatus(Long originalPostId, String status);
}