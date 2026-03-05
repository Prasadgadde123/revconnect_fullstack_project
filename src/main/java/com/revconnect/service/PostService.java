// src/main/java/com/revconnect/service/PostService.java
package com.revconnect.service;

import com.revconnect.dto.request.CreatePostRequest;
import com.revconnect.dto.request.UpdatePostRequest;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.PostResponse;
import com.revconnect.model.post.Post;
import com.revconnect.model.user.User;
import com.revconnect.repository.FollowRepository;
import com.revconnect.repository.PostRepository;
import com.revconnect.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       FollowRepository followRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns the currently logged-in User from the security context. */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /** Maps a Post entity to a PostResponse DTO. */
    private PostResponse toResponse(Post post, Long viewerUserId) {
        PostResponse r = new PostResponse();
        r.setId(post.getId());
        r.setContent(post.getContent());
        r.setHashtags(post.getHashtags());
        r.setHashtagList(post.getHashtagList());
        r.setPostType(post.getPostType());
        r.setLikesCount(post.getLikesCount() != null ? post.getLikesCount() : 0);
        r.setCommentsCount(post.getCommentsCount() != null ? post.getCommentsCount() : 0);
        r.setRepostsCount(post.getRepostsCount() != null ? post.getRepostsCount() : 0);
        r.setCreatedAt(post.getCreatedAt());
        r.setUpdatedAt(post.getUpdatedAt());

        // Author
        if (post.getUser() != null) {
            r.setAuthorId(post.getUser().getId());
            r.setAuthorUsername(post.getUser().getUsername());
            r.setAuthorFullName(post.getUser().getFullName());
            r.setAuthorProfilePicture(post.getUser().getProfilePicture());
        }

        // Repost original
        if (post.getOriginalPost() != null) {
            Post orig = post.getOriginalPost();
            r.setOriginalPostId(orig.getId());
            r.setOriginalContent(orig.getContent());
            r.setRepostComment(post.getRepostComment());
            if (orig.getUser() != null) {
                r.setOriginalAuthorUsername(orig.getUser().getUsername());
                r.setOriginalAuthorFullName(orig.getUser().getFullName());
            }
        }

        // Viewer context
        if (viewerUserId != null && post.getUser() != null) {
            r.setOwnPost(post.getUser().getId().equals(viewerUserId));
        }
        // likedByMe: will be populated by LikeService in Module 3 (Social Interactions)
        r.setLikedByMe(false);

        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public PostResponse createPost(CreatePostRequest request) {
        User currentUser = getCurrentUser();

        Post post = new Post(currentUser, request.getContent().trim());

        // Process hashtags: strip leading #, lowercase, store comma-separated
        if (request.getHashtags() != null && !request.getHashtags().isBlank()) {
            String cleaned = processHashtags(request.getHashtags());
            post.setHashtags(cleaned);
        }

        post = postRepository.save(post);
        return toResponse(post, currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findByIdAndStatus(postId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Post not found or has been deleted"));
        return toResponse(post, currentUser.getId());
    }

    /** Get all active posts by a specific user (for profile page), paginated. */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUser(Long userId, int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "ACTIVE", pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    /** Get own posts (convenience method for profile). */
    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(int page, int size) {
        User currentUser = getCurrentUser();
        return getPostsByUser(currentUser.getId(), page, size);
    }

    /**
     * Personalized feed: posts from followed users + own posts, newest first.
     * NOTE: Follow integration requires FollowRepository.findFollowingIds(userId).
     *       If that method doesn't exist yet, the feed falls back to own posts only.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedPosts(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        // Collect ids of users whose posts to show
        List<Long> feedUserIds = new ArrayList<>();
        feedUserIds.add(currentUser.getId()); // always include own posts

        try {
            // Use findByFollower(User, Pageable) — fetch up to 500 followed users for the feed
            org.springframework.data.domain.Pageable allFollowing =
                    PageRequest.of(0, 500);
            followRepository.findByFollower(currentUser, allFollowing)
                    .forEach(f -> feedUserIds.add(f.getFollowing().getId()));
        } catch (Exception e) {
            // Follow data not available yet — show own posts only
        }

        Page<Post> posts = postRepository.findFeedPosts(feedUserIds, pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    /** Search posts by hashtag. */
    @Transactional(readOnly = true)
    public Page<PostResponse> searchByHashtag(String hashtag, int page, int size) {
        User currentUser = getCurrentUser();
        String tag = hashtag.startsWith("#") ? hashtag.substring(1).toLowerCase() : hashtag.toLowerCase();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByHashtag(tag, pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    /** Trending posts (most liked). */
    @Transactional(readOnly = true)
    public Page<PostResponse> getTrendingPosts(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findTrendingPosts(pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────────────────────────

    public PostResponse updatePost(Long postId, UpdatePostRequest request) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findByIdAndStatus(postId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Post not found or has been deleted"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to edit this post");
        }

        post.setContent(request.getContent().trim());

        if (request.getHashtags() != null && !request.getHashtags().isBlank()) {
            post.setHashtags(processHashtags(request.getHashtags()));
        } else {
            post.setHashtags(null);
        }

        post = postRepository.save(post);
        return toResponse(post, currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    public MessageResponse deletePost(Long postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findByIdAndStatus(postId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Post not found or has been deleted"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        post.softDelete();
        postRepository.save(post);

        return new MessageResponse("Post deleted successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REPOST
    // ─────────────────────────────────────────────────────────────────────────

    public PostResponse repostPost(Long originalPostId, String comment) {
        User currentUser = getCurrentUser();

        Post originalPost = postRepository.findByIdAndStatus(originalPostId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Original post not found or has been deleted"));

        // Build content: use comment if provided, otherwise a default note
        String content = (comment != null && !comment.isBlank())
                ? comment.trim()
                : "Reposted from @" + originalPost.getUser().getUsername();

        Post repost = new Post(currentUser, content);
        repost.setPostType("REPOST");
        repost.setOriginalPost(originalPost);
        repost.setRepostComment(comment != null ? comment.trim() : null);

        repost = postRepository.save(repost);

        // Increment original post's repost counter
        originalPost.setRepostsCount(
                (originalPost.getRepostsCount() != null ? originalPost.getRepostsCount() : 0) + 1
        );
        postRepository.save(originalPost);

        return toResponse(repost, currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATS HELPERS (called by LikeService / CommentService in Module 3)
    // ─────────────────────────────────────────────────────────────────────────

    public void incrementLikes(Long postId) {
        postRepository.findById(postId).ifPresent(p -> {
            p.setLikesCount((p.getLikesCount() != null ? p.getLikesCount() : 0) + 1);
            postRepository.save(p);
        });
    }

    public void decrementLikes(Long postId) {
        postRepository.findById(postId).ifPresent(p -> {
            int current = p.getLikesCount() != null ? p.getLikesCount() : 0;
            p.setLikesCount(Math.max(0, current - 1));
            postRepository.save(p);
        });
    }

    public void incrementComments(Long postId) {
        postRepository.findById(postId).ifPresent(p -> {
            p.setCommentsCount((p.getCommentsCount() != null ? p.getCommentsCount() : 0) + 1);
            postRepository.save(p);
        });
    }

    public void decrementComments(Long postId) {
        postRepository.findById(postId).ifPresent(p -> {
            int current = p.getCommentsCount() != null ? p.getCommentsCount() : 0;
            p.setCommentsCount(Math.max(0, current - 1));
            postRepository.save(p);
        });
    }

    /** Count of active posts for a user — used by UserController for stats. */
    @Transactional(readOnly = true)
    public long countPostsByUser(Long userId) {
        return postRepository.countByUserIdAndStatus(userId, "ACTIVE");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cleans raw hashtag input.
     * Accepts: "java, #Spring, BOOT" → stored as "java,spring,boot"
     */
    private String processHashtags(String raw) {
        if (raw == null || raw.isBlank()) return null;
        StringBuilder sb = new StringBuilder();
        for (String tag : raw.split("[,\\s]+")) {
            String t = tag.trim().replaceAll("^#+", "").toLowerCase();
            if (!t.isEmpty()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(t);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}