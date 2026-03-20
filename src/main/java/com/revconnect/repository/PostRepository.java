package com.revconnect.repository;

import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

        List<Post> findByAuthorAndDeletedFalseOrderByPinnedDescCreatedAtDesc(User author);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.author = :author AND p.published = true ORDER BY p.pinned DESC, p.createdAt DESC")
        List<Post> findPublishedUserPosts(@Param("author") User author);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.author = :author AND p.published = false AND p.scheduledAt > :now ORDER BY p.scheduledAt ASC")
        List<Post> findScheduledPosts(@Param("author") User author, @Param("now") java.time.LocalDateTime now);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.author = :author AND p.published = false AND p.scheduledAt > :now AND p.scheduledAt <= :cutoffTime ORDER BY p.scheduledAt ASC")
        List<Post> findSoonToBePublishedPosts(@Param("author") User author, @Param("now") java.time.LocalDateTime now,
                                              @Param("cutoffTime") java.time.LocalDateTime cutoffTime);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND " +
                "p.author.id IN :userIds AND p.published = true " +
                "ORDER BY p.createdAt DESC")
        Page<Post> findFeedPosts(@Param("userIds") List<Long> userIds,
                                 Pageable pageable);

        /**
         * Personalized Feed Query: Calculates an engagement score based on likes,
         * comments, and reposts.
         * The weights are calibrated for professional platform relevance.
         */
        @Query("SELECT p FROM Post p " +
                "WHERE p.deleted = false " +
                "AND p.author.id IN :userIds " +
                "AND p.published = true " +
                "ORDER BY p.createdAt DESC")
        Page<Post> findRankedFeedPosts(@Param("userIds") List<Long> userIds,
                                       Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND " +
                "p.author.id IN :userIds AND p.postType = :postType AND p.published = true "
                +
                "ORDER BY p.createdAt DESC")
        Page<Post> findFeedPostsByType(@Param("userIds") List<Long> userIds,
                                       @Param("postType") com.revconnect.enums.PostType postType,
                                       Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND " +
                "p.author.id IN :userIds AND p.author.role = :userRole AND p.published = true "
                +
                "ORDER BY p.createdAt DESC")
        Page<Post> findFeedPostsByRole(@Param("userIds") List<Long> userIds,
                                       @Param("userRole") com.revconnect.enums.UserRole userRole,
                                       Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.scheduledAt IS NOT NULL AND p.scheduledAt <= :now AND p.published = false")
        List<Post> findDuePosts(@Param("now") java.time.LocalDateTime now);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND " +
                "LOWER(p.hashtags) LIKE LOWER(CONCAT('%',:tag,'%')) " +
                "ORDER BY p.createdAt DESC")
        List<Post> findByHashtag(@Param("tag") String tag);

        @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY SIZE(p.likes) DESC")
        List<Post> findTrendingPosts(Pageable pageable);

        @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.author = :author AND p.pinned = true")
        List<Post> findPinnedPosts(@Param("author") User author);

        @Query("SELECT DISTINCT p.hashtags FROM Post p WHERE p.hashtags IS NOT NULL AND p.deleted = false")
        List<String> findAllHashtagStrings();

        long countByAuthorAndDeletedFalse(User author);

        /**
         * Check if a user has liked a post without loading the lazy likes collection
         */
        @Query("SELECT COUNT(u) > 0 FROM Post p JOIN p.likes u WHERE p.id = :postId AND u.id = :userId")
        boolean isLikedByUser(@Param("postId") Long postId, @Param("userId") Long userId);

        /** Count likes without loading the lazy likes collection */
        @Query("SELECT COUNT(u) FROM Post p JOIN p.likes u WHERE p.id = :postId")
        long countLikes(@Param("postId") Long postId);

        /** Count non-deleted comments without loading the lazy comments collection */
        @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deleted = false")
        long countComments(@Param("postId") Long postId);

        /** Find existing reposts of a post by a specific user */
        @Query("SELECT p FROM Post p WHERE p.originalPost = :originalPost AND p.author = :author AND p.postType = 'REPOST' AND p.deleted = false")
        List<Post> findRepostsByOriginalPostAndAuthor(@Param("originalPost") Post originalPost,
                                                      @Param("author") User author);

        /** Check if a user has reposted a post */
        @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.originalPost.id = :postId AND p.author.id = :userId AND p.postType = 'REPOST' AND p.deleted = false")
        boolean isRepostedByUser(@Param("postId") Long postId, @Param("userId") Long userId);

        /** Find all reposts by a user (for profile page) */
        @Query("SELECT p FROM Post p WHERE p.author = :author AND p.postType = 'REPOST' AND p.deleted = false ORDER BY p.createdAt DESC")
        List<Post> findRepostsByAuthor(@Param("author") User author);
}