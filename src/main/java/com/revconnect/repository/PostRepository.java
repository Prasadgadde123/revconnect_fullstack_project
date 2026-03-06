// src/main/java/com/revconnect/repository/PostRepository.java
package com.revconnect.repository;

import com.revconnect.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ─── Own posts (profile page) ─────────────────────────────────────────────

    Page<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    List<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    long countByUserIdAndStatus(Long userId, String status);

    // ─── Feed posts ──────────────────────────────────────────────────────────

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.status = 'ACTIVE' " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findFeedPosts(@Param("userIds") List<Long> userIds, Pageable pageable);

    // ─── Single post ─────────────────────────────────────────────────────────

    Optional<Post> findByIdAndStatus(Long id, String status);

    // ─── Hashtag search ──────────────────────────────────────────────────────

    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' AND p.hashtags LIKE %:hashtag% " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    // ─── Trending ────────────────────────────────────────────────────────────

    @Query("SELECT p FROM Post p WHERE p.status = 'ACTIVE' ORDER BY p.likesCount DESC, p.createdAt DESC")
    Page<Post> findTrendingPosts(Pageable pageable);

    // ─── Reposts ─────────────────────────────────────────────────────────────

    List<Post> findByOriginalPostIdAndStatus(Long originalPostId, String status);

    long countByOriginalPostIdAndStatus(Long originalPostId, String status);

    // ─── FEATURE 3: Pinned posts ─────────────────────────────────────────────

    /** All pinned ACTIVE posts for a user, newest pin first. */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.status = 'ACTIVE' " +
            "AND p.isPinned = true ORDER BY p.pinnedAt DESC")
    List<Post> findPinnedPostsByUserId(@Param("userId") Long userId);

    /** Count how many posts a user has pinned. */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.status = 'ACTIVE' AND p.isPinned = true")
    long countPinnedByUserId(@Param("userId") Long userId);

    // ─── FEATURE 2: Scheduled posts ──────────────────────────────────────────

    /** All scheduled posts for a user, soonest first. */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.status = 'SCHEDULED' " +
            "ORDER BY p.scheduledAt ASC")
    List<Post> findScheduledPostsByUserId(@Param("userId") Long userId);

    /** Scheduled posts that are now ready to be published (scheduler job query). */
    @Query("SELECT p FROM Post p WHERE p.status = 'SCHEDULED' AND p.scheduledAt <= :now")
    List<Post> findPostsDueToPublish(@Param("now") LocalDateTime now);

    // ─── FEATURE 1: Tagged products ──────────────────────────────────────────

    /** Active posts that tag a specific product/service. */
    @Query("SELECT p FROM Post p JOIN p.taggedProducts ps WHERE ps.id = :productId AND p.status = 'ACTIVE' " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findByTaggedProductId(@Param("productId") Long productId, Pageable pageable);
}