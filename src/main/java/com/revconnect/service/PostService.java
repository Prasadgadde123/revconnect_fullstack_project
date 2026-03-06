// src/main/java/com/revconnect/service/PostService.java
package com.revconnect.service;

import com.revconnect.dto.request.CreatePostRequest;
import com.revconnect.dto.request.UpdatePostRequest;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.PostResponse;
import com.revconnect.model.post.Post;
import com.revconnect.model.user.ProductService;
import com.revconnect.model.user.User;
import com.revconnect.model.user.BusinessProfile;
import com.revconnect.repository.BusinessProfileRepository;
import com.revconnect.repository.FollowRepository;
import com.revconnect.repository.PostRepository;
import com.revconnect.repository.ProductServiceRepository;
import com.revconnect.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    private static final Logger logger = LogManager.getLogger(PostService.class);

    // Max pins per user — keeps the profile clean
    private static final int MAX_PINNED_POSTS = 3;

    private final PostRepository            postRepository;
    private final UserRepository            userRepository;
    private final FollowRepository          followRepository;
    private final ProductServiceRepository  productServiceRepository;
    private final BusinessProfileRepository businessProfileRepository;

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       FollowRepository followRepository,
                       ProductServiceRepository productServiceRepository,
                       BusinessProfileRepository businessProfileRepository) {
        this.postRepository            = postRepository;
        this.userRepository            = userRepository;
        this.followRepository          = followRepository;
        this.productServiceRepository  = productServiceRepository;
        this.businessProfileRepository = businessProfileRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /** Maps a Post entity → PostResponse DTO. */
    private PostResponse toResponse(Post post, Long viewerUserId) {
        PostResponse r = new PostResponse();
        r.setId(post.getId());
        r.setContent(post.getContent());
        r.setHashtags(post.getHashtags());
        r.setHashtagList(post.getHashtagList());
        r.setPostType(post.getPostType());
        r.setLikesCount(post.getLikesCount()    != null ? post.getLikesCount()    : 0);
        r.setCommentsCount(post.getCommentsCount() != null ? post.getCommentsCount() : 0);
        r.setRepostsCount(post.getRepostsCount()  != null ? post.getRepostsCount()  : 0);
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

        // ─── FEATURE 1: Tagged products ──────────────────────
        if (post.getTaggedProducts() != null && !post.getTaggedProducts().isEmpty()) {
            List<PostResponse.TaggedProductDto> dtos = post.getTaggedProducts().stream()
                    .map(ps -> {
                        PostResponse.TaggedProductDto dto = new PostResponse.TaggedProductDto();
                        dto.setId(ps.getId());
                        dto.setName(ps.getName());
                        dto.setItemType(ps.getItemType());
                        dto.setImageUrl(ps.getImageUrl());
                        dto.setPurchaseUrl(ps.getPurchaseUrl());
                        if (ps.getPrice() != null) {
                            dto.setPrice(ps.getCurrency() + " " + ps.getPrice().toPlainString());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
            r.setTaggedProducts(dtos);
        } else {
            r.setTaggedProducts(Collections.emptyList());
        }

        // ─── FEATURE 2: Scheduled ────────────────────────────
        r.setScheduledAt(post.getScheduledAt());
        r.setScheduled("SCHEDULED".equals(post.getStatus()));

        // ─── FEATURE 3: Pinned ──────────────────────────────
        r.setPinned(Boolean.TRUE.equals(post.getIsPinned()));
        r.setPinnedAt(post.getPinnedAt());

        // Viewer context
        if (viewerUserId != null && post.getUser() != null) {
            r.setOwnPost(post.getUser().getId().equals(viewerUserId));
        }
        r.setLikedByMe(false); // populated by LikeService (Module 3)

        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────────────────────────

    public PostResponse createPost(CreatePostRequest request) {
        User currentUser = getCurrentUser();

        Post post = new Post(currentUser, request.getContent().trim());

        // Hashtags
        if (request.getHashtags() != null && !request.getHashtags().isBlank()) {
            post.setHashtags(processHashtags(request.getHashtags()));
        }

        // ─── FEATURE 1: Tag products/services ────────────────
        if (request.getTaggedProductIds() != null && !request.getTaggedProductIds().isEmpty()) {
            Set<ProductService> tagged = resolveTaggedProducts(request.getTaggedProductIds(), currentUser);
            post.setTaggedProducts(tagged);
            // Mark as promotional if products are tagged
            post.setPostType("PROMOTIONAL");
        }

        // ─── FEATURE 2: Schedule ─────────────────────────────
        if (request.getScheduledAt() != null) {
            if (request.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Scheduled time must be in the future");
            }
            post.setScheduledAt(request.getScheduledAt());
            post.setStatus("SCHEDULED");
            logger.info("Post scheduled for {} by user {}", request.getScheduledAt(), currentUser.getUsername());
        }

        // ─── FEATURE 3: Pin immediately on creation ──────────
        if (Boolean.TRUE.equals(request.getPinPost()) && post.isActive()) {
            enforcePinLimit(currentUser.getId());
            post.pin();
        }

        post = postRepository.save(post);
        logger.info("Post created [id={}, type={}, status={}] by {}",
                post.getId(), post.getPostType(), post.getStatus(), currentUser.getUsername());
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

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(int page, int size) {
        User currentUser = getCurrentUser();
        return getPostsByUser(currentUser.getId(), page, size);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeedPosts(int page, int size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        List<Long> feedUserIds = new ArrayList<>();
        feedUserIds.add(currentUser.getId());

        try {
            Pageable allFollowing = PageRequest.of(0, 500);
            followRepository.findByFollower(currentUser, allFollowing)
                    .forEach(f -> feedUserIds.add(f.getFollowing().getId()));
        } catch (Exception e) {
            // Fall back to own posts only
        }

        Page<Post> posts = postRepository.findFeedPosts(feedUserIds, pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

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

        // Allow editing ACTIVE or SCHEDULED posts
        Post post = postRepository.findById(postId)
                .filter(p -> "ACTIVE".equals(p.getStatus()) || "SCHEDULED".equals(p.getStatus()))
                .orElseThrow(() -> new RuntimeException("Post not found or has been deleted"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to edit this post");
        }

        post.setContent(request.getContent().trim());
        post.setHashtags(request.getHashtags() != null && !request.getHashtags().isBlank()
                ? processHashtags(request.getHashtags())
                : null);

        // ─── FEATURE 1: Update tagged products ───────────────
        if (request.getTaggedProductIds() != null) {
            Set<ProductService> tagged = resolveTaggedProducts(request.getTaggedProductIds(), currentUser);
            post.setTaggedProducts(tagged);
            if (!tagged.isEmpty() && !"REPOST".equals(post.getPostType())) {
                post.setPostType("PROMOTIONAL");
            } else if (tagged.isEmpty() && "PROMOTIONAL".equals(post.getPostType())) {
                post.setPostType("TEXT");
            }
        }

        // ─── FEATURE 2: Update schedule ──────────────────────
        if (request.getScheduledAt() != null) {
            if (request.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Scheduled time must be in the future");
            }
            post.setScheduledAt(request.getScheduledAt());
            post.setStatus("SCHEDULED");
        } else if (post.isScheduled()) {
            // Clear schedule → publish now
            post.setScheduledAt(null);
            post.setStatus("ACTIVE");
        }

        post = postRepository.save(post);
        return toResponse(post, currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    public MessageResponse deletePost(Long postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findById(postId)
                .filter(p -> !"DELETED".equals(p.getStatus()))
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

        String content = (comment != null && !comment.isBlank())
                ? comment.trim()
                : "Reposted from @" + originalPost.getUser().getUsername();

        Post repost = new Post(currentUser, content);
        repost.setPostType("REPOST");
        repost.setOriginalPost(originalPost);
        repost.setRepostComment(comment != null ? comment.trim() : null);
        repost = postRepository.save(repost);

        originalPost.setRepostsCount(
                (originalPost.getRepostsCount() != null ? originalPost.getRepostsCount() : 0) + 1);
        postRepository.save(originalPost);

        return toResponse(repost, currentUser.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FEATURE 3: PIN / UNPIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pins a post to the top of the user's profile.
     * Enforces MAX_PINNED_POSTS limit (default: 3).
     */
    public PostResponse pinPost(Long postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findByIdAndStatus(postId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Post not found or already deleted"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You can only pin your own posts");
        }

        if (Boolean.TRUE.equals(post.getIsPinned())) {
            throw new RuntimeException("Post is already pinned");
        }

        enforcePinLimit(currentUser.getId());
        post.pin();
        post = postRepository.save(post);

        logger.info("Post {} pinned by user {}", postId, currentUser.getUsername());
        return toResponse(post, currentUser.getId());
    }

    /** Unpins a post. */
    public PostResponse unpinPost(Long postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findByIdAndStatus(postId, "ACTIVE")
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You can only unpin your own posts");
        }

        post.unpin();
        post = postRepository.save(post);

        logger.info("Post {} unpinned by user {}", postId, currentUser.getUsername());
        return toResponse(post, currentUser.getId());
    }

    /** Returns all pinned posts for a user's profile. */
    @Transactional(readOnly = true)
    public List<PostResponse> getPinnedPosts(Long userId) {
        User currentUser = getCurrentUser();
        return postRepository.findPinnedPostsByUserId(userId).stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FEATURE 2: SCHEDULED POSTS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all scheduled posts for the current user (for the "Scheduled Posts" management page).
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getMyScheduledPosts() {
        User currentUser = getCurrentUser();
        return postRepository.findScheduledPostsByUserId(currentUser.getId()).stream()
                .map(p -> toResponse(p, currentUser.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Scheduled job: runs every minute, publishes posts whose scheduled_at has passed.
     * Enable scheduling in your main app class: @EnableScheduling
     */
    @Scheduled(fixedDelay = 60_000) // every 60 seconds
    public void publishScheduledPosts() {
        List<Post> due = postRepository.findPostsDueToPublish(LocalDateTime.now());
        if (!due.isEmpty()) {
            logger.info("Publishing {} scheduled post(s)", due.size());
            due.forEach(p -> {
                p.setStatus("ACTIVE");
                p.setScheduledAt(null);
                postRepository.save(p);
                logger.info("Scheduled post {} published", p.getId());
            });
        }
    }

    /**
     * Cancel (delete) a scheduled post before it goes live.
     */
    public MessageResponse cancelScheduledPost(Long postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findById(postId)
                .filter(p -> "SCHEDULED".equals(p.getStatus()))
                .orElseThrow(() -> new RuntimeException("Scheduled post not found"));

        if (!post.isOwnedBy(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to cancel this post");
        }

        post.softDelete();
        postRepository.save(post);
        return new MessageResponse("Scheduled post cancelled successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FEATURE 1: TAG PRODUCTS — search available products for dropdown
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns ACTIVE products/services that the current user's business profile owns.
     * Used to populate the "Tag Product" dropdown on the create/edit post form.
     */
    @Transactional(readOnly = true)
    public List<PostResponse.TaggedProductDto> getMyTaggableProducts() {
        User currentUser = getCurrentUser();

        // Look up business profile via repository (User has no direct relationship)
        java.util.Optional<BusinessProfile> bpOpt =
                businessProfileRepository.findByUserId(currentUser.getId());
        if (bpOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Long bpId = bpOpt.get().getId();
        return productServiceRepository
                .findByBusinessProfileIdAndStatusOrderByDisplayOrderAsc(bpId, "ACTIVE")
                .stream()
                .map(ps -> {
                    PostResponse.TaggedProductDto dto = new PostResponse.TaggedProductDto();
                    dto.setId(ps.getId());
                    dto.setName(ps.getName());
                    dto.setItemType(ps.getItemType());
                    dto.setImageUrl(ps.getImageUrl());
                    dto.setPurchaseUrl(ps.getPurchaseUrl());
                    if (ps.getPrice() != null) {
                        dto.setPrice(ps.getCurrency() + " " + ps.getPrice().toPlainString());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATS HELPERS
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

    @Transactional(readOnly = true)
    public long countPostsByUser(Long userId) {
        return postRepository.countByUserIdAndStatus(userId, "ACTIVE");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

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

    /**
     * Validates that all given product IDs belong to the current user's business profile.
     * Prevents tagging other businesses' products.
     */
    private Set<ProductService> resolveTaggedProducts(List<Long> ids, User owner) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();

        // Look up business profile via repository (User has no direct relationship)
        BusinessProfile bp = businessProfileRepository.findByUserId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Only business/creator accounts can tag products"));

        Set<ProductService> result = new HashSet<>();
        for (Long id : ids) {
            ProductService ps = productServiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product/service not found: " + id));
            if (!ps.getBusinessProfile().getId().equals(bp.getId())) {
                throw new RuntimeException("You can only tag your own products/services");
            }
            result.add(ps);
        }
        return result;
    }

    /** Throws if the user already has MAX_PINNED_POSTS pinned. */
    private void enforcePinLimit(Long userId) {
        long count = postRepository.countPinnedByUserId(userId);
        if (count >= MAX_PINNED_POSTS) {
            throw new RuntimeException(
                    "You can only pin up to " + MAX_PINNED_POSTS + " posts. Please unpin one first.");
        }
    }
}