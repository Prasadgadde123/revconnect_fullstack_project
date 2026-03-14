package com.revconnect.service;

import com.revconnect.dto.PostCreateDTO;
import com.revconnect.entity.Comment;
import com.revconnect.entity.Post;
import com.revconnect.entity.SharedPost;
import com.revconnect.entity.User;
import com.revconnect.enums.NotificationType;
import com.revconnect.enums.PostType;
import com.revconnect.exception.ResourceNotFoundException;
import com.revconnect.repository.CommentRepository;
import com.revconnect.repository.PostRepository;
import com.revconnect.repository.SharedPostRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private static final Logger logger = LogManager.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SharedPostRepository sharedPostRepository;
    private final NotificationService notificationService;
    private final ConnectionService connectionService;
    private final UserService userService;

    public Post createPost(User author, PostCreateDTO dto) {
        logger.info("Creating post for user: {}", author.getUsername());
        logger.debug("Post details - type: {}, scheduledAt: {}, hasHashtags: {}",
                dto.getPostType(), dto.getScheduledAt(), dto.getHashtags() != null);

        Post post = Post.builder()
                .content(dto.getContent())
                .author(author)
                .hashtags(sanitizeHashtags(dto.getHashtags()))
                .postType(dto.getPostType() != null ? dto.getPostType() : PostType.REGULAR)
                .ctaButtonText(dto.getCtaButtonText())
                .ctaButtonUrl(dto.getCtaButtonUrl())
                .taggedProducts(parseTaggedProducts(dto.getTaggedProducts()))
                .scheduledAt(dto.getScheduledAt())
                .published(dto.getScheduledAt() == null || dto.getScheduledAt().isBefore(java.time.LocalDateTime.now()))
                .build();

        Post saved = postRepository.save(post);
        logger.info("Post created successfully with id: {} by user: {}", saved.getId(), author.getUsername());
        return saved;
    }

    public Post updatePost(Long postId, User currentUser, PostCreateDTO dto) {
        logger.info("Updating post id: {} by user: {}", postId, currentUser.getUsername());

        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser)) {
            logger.warn("Unauthorized update attempt on post id: {} by user: {}", postId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        post.setContent(dto.getContent());
        post.setHashtags(sanitizeHashtags(dto.getHashtags()));
        if (dto.getCtaButtonText() != null)
            post.setCtaButtonText(dto.getCtaButtonText());
        if (dto.getCtaButtonUrl() != null)
            post.setCtaButtonUrl(dto.getCtaButtonUrl());
        if (dto.getTaggedProducts() != null)
            post.setTaggedProducts(parseTaggedProducts(dto.getTaggedProducts()));
        post.setScheduledAt(dto.getScheduledAt());

        Post updated = postRepository.save(post);
        logger.info("Post id: {} updated successfully", postId);
        return updated;
    }

    private List<String> parseTaggedProducts(String taggedProductsStr) {
        if (taggedProductsStr == null || taggedProductsStr.isBlank()) {
            return new ArrayList<>();
        }
        List<String> products = Arrays.stream(taggedProductsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        logger.debug("Parsed {} tagged products", products.size());
        return products;
    }

    public void deletePost(Long postId, User currentUser) {
        logger.info("Delete requested for post id: {} by user: {}", postId, currentUser.getUsername());

        Post post = getPostById(postId);
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != com.revconnect.enums.UserRole.ADMIN) {
            logger.warn("Unauthorized delete attempt on post id: {} by user: {}", postId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        post.setDeleted(true);
        postRepository.save(post);
        logger.info("Post id: {} soft-deleted by user: {}", postId, currentUser.getUsername());
    }

    public void adminDeletePost(Long postId) {
        logger.info("Admin deleting post id: {}", postId);
        Post post = getPostById(postId);
        post.setDeleted(true);
        postRepository.save(post);
        logger.info("Post id: {} deleted by admin", postId);
    }

    public Post toggleLike(Long postId, User user) {
        Post post = getPostById(postId);
        if (post.getLikes().contains(user)) {
            post.getLikes().remove(user);
            logger.debug("User: {} unliked post id: {}", user.getUsername(), postId);
        } else {
            post.getLikes().add(user);
            logger.debug("User: {} liked post id: {}", user.getUsername(), postId);
            if (!post.getAuthor().equals(user)) {
                notificationService.createNotification(
                        post.getAuthor(), user,
                        NotificationType.POST_LIKED,
                        user.getDisplayNameOrUsername() + " liked your post",
                        "/post/" + postId);
            }
        }
        return postRepository.save(post);
    }

    public Comment addComment(Long postId, User author, String content) {
        logger.info("User: {} adding comment to post id: {}", author.getUsername(), postId);

        Post post = getPostById(postId);
        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .build();
        Comment saved = commentRepository.save(comment);
        logger.info("Comment id: {} added to post id: {} by user: {}", saved.getId(), postId, author.getUsername());

        if (!post.getAuthor().equals(author)) {
            notificationService.createNotification(
                    post.getAuthor(), author,
                    NotificationType.POST_COMMENTED,
                    author.getDisplayNameOrUsername() + " commented on your post",
                    "/post/" + postId);
        }
        return saved;
    }

    public void deleteComment(Long commentId, User currentUser) {
        logger.info("User: {} deleting comment id: {}", currentUser.getUsername(), commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.error("Comment not found with id: {}", commentId);
                    return new ResourceNotFoundException("Comment not found");
                });

        if (!comment.getAuthor().equals(currentUser)) {
            logger.warn("Unauthorized delete attempt on comment id: {} by user: {}", commentId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
        logger.info("Comment id: {} deleted by user: {}", commentId, currentUser.getUsername());
    }

    public Post repost(Long originalPostId, User author) {
        logger.info("User: {} attempting repost of post id: {}", author.getUsername(), originalPostId);

        Post original = getPostById(originalPostId);
        List<Post> existingReposts = postRepository.findRepostsByOriginalPostAndAuthor(original, author);

        if (!existingReposts.isEmpty()) {
            logger.info("User: {} undoing repost of post id: {}", author.getUsername(), originalPostId);
            for (Post r : existingReposts) {
                r.setDeleted(true);
            }
            postRepository.saveAll(existingReposts);
            return null;
        }

        Post repost = Post.builder()
                .content(original.getContent())
                .author(author)
                .postType(PostType.REPOST)
                .originalPost(original)
                .hashtags(original.getHashtags())
                .build();
        Post saved = postRepository.save(repost);
        logger.info("Repost created with id: {} by user: {} for original post id: {}", saved.getId(), author.getUsername(), originalPostId);

        if (!original.getAuthor().equals(author)) {
            notificationService.createNotification(
                    original.getAuthor(), author,
                    NotificationType.POST_SHARED,
                    author.getDisplayNameOrUsername() + " shared your post",
                    "/post/" + original.getId());
        }
        return saved;
    }

    public void togglePin(Long postId, User currentUser) {
        logger.info("User: {} toggling pin on post id: {}", currentUser.getUsername(), postId);

        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser)) {
            logger.warn("Unauthorized pin attempt on post id: {} by user: {}", postId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        boolean newPinnedStatus = !post.isPinned();

        if (newPinnedStatus) {
            List<Post> pinnedPosts = postRepository.findPinnedPosts(currentUser);
            for (Post p : pinnedPosts) {
                p.setPinned(false);
            }
            postRepository.saveAll(pinnedPosts);
            logger.debug("Unpinned {} existing pinned posts for user: {}", pinnedPosts.size(), currentUser.getUsername());
        }

        post.setPinned(newPinnedStatus);
        postRepository.save(post);
        logger.info("Post id: {} pin status set to: {} by user: {}", postId, newPinnedStatus, currentUser.getUsername());
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        logger.debug("Fetching post by id: {}", id);
        return postRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> {
                    logger.error("Post not found or deleted with id: {}", id);
                    return new ResourceNotFoundException("Post not found");
                });
    }

    @Transactional(readOnly = true)
    public List<Post> getUserPosts(User user) {
        logger.debug("Fetching posts for user: {}", user.getUsername());
        return postRepository.findPublishedUserPosts(user);
    }

    @Transactional(readOnly = true)
    public List<Post> getPublishedUserPosts(User user) {
        logger.debug("Fetching published posts for user: {}", user.getUsername());
        return postRepository.findPublishedUserPosts(user);
    }

    @Transactional(readOnly = true)
    public List<Post> getUserReposts(User user) {
        logger.debug("Fetching reposts for user: {}", user.getUsername());
        return postRepository.findRepostsByAuthor(user);
    }

    @Transactional(readOnly = true)
    public List<Post> getScheduledPosts(User author) {
        logger.debug("Fetching scheduled posts for user: {}", author.getUsername());
        return postRepository.findScheduledPosts(author, java.time.LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Post> getSoonToBePublishedPosts(User author) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime cutoffTime = now.plusMinutes(5);
        logger.debug("Fetching soon-to-publish posts for user: {} between {} and {}", author.getUsername(), now, cutoffTime);
        return postRepository.findSoonToBePublishedPosts(author, now, cutoffTime);
    }

    @Transactional(readOnly = true)
    public Page<Post> getFeedPosts(User currentUser, int page, String type, String userRole) {
        logger.info("Fetching feed for user: {}, page: {}, type: {}, role: {}", currentUser.getUsername(), page, type, userRole);

        currentUser = userService.findByUsername(currentUser.getUsername());
        List<User> connections = connectionService.getConnections(currentUser);
        List<User> following = new ArrayList<>(currentUser.getFollowing());
        Set<User> feedUsers = new HashSet<>();
        feedUsers.addAll(connections);
        feedUsers.addAll(following);
        feedUsers.add(currentUser);

        List<Long> userIds = feedUsers.stream().map(User::getId).collect(java.util.stream.Collectors.toList());
        logger.debug("Feed includes {} users for user: {}", feedUsers.size(), currentUser.getUsername());

        PageRequest pageable = PageRequest.of(page, 20);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (type != null && !type.isBlank()) {
            return postRepository.findFeedPostsByType(userIds,
                    com.revconnect.enums.PostType.valueOf(type.toUpperCase()), pageable);
        } else if (userRole != null && !userRole.isBlank()) {
            return postRepository.findFeedPostsByRole(userIds,
                    com.revconnect.enums.UserRole.valueOf(userRole.toUpperCase()), pageable);
        }
        return postRepository.findFeedPosts(userIds, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPersonalizedFeed(User currentUser, int page) {
        logger.info("Fetching personalized ranked feed for user: {}, page: {}", currentUser.getUsername(), page);

        currentUser = userService.findByUsername(currentUser.getUsername());
        List<User> connections = connectionService.getConnections(currentUser);
        List<User> following = new ArrayList<>(currentUser.getFollowing());
        Set<User> feedUsers = new HashSet<>();
        feedUsers.addAll(connections);
        feedUsers.addAll(following);
        feedUsers.add(currentUser);

        List<Long> userIds = feedUsers.stream().map(User::getId).collect(Collectors.toList());
        logger.debug("Personalized feed for user: {} includes {} users", currentUser.getUsername(), feedUsers.size());

        PageRequest pageable = PageRequest.of(page, 20);
        return postRepository.findRankedFeedPosts(userIds, pageable);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        long count = postRepository.countLikes(postId);
        logger.debug("Post id: {} has {} likes", postId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        long count = commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(getPostById(postId)).size();
        logger.debug("Post id: {} has {} comments", postId, count);
        return count;
    }

    public void shareToSelectedUsers(Long postId, User sharer, List<Long> userIds, String message) {
        logger.info("User: {} sharing post id: {} to {} users", sharer.getUsername(), postId, userIds.size());

        Post post = getPostById(postId);
        List<User> connections = connectionService.getConnections(sharer);
        int sharedCount = 0;

        for (User conn : connections) {
            if (userIds.contains(conn.getId())) {
                SharedPost shared = SharedPost.builder()
                        .post(post)
                        .sender(sharer)
                        .recipient(conn)
                        .message(message)
                        .build();
                sharedPostRepository.save(shared);

                String notificationMsg = sharer.getDisplayNameOrUsername() + " shared a post with you";
                if (message != null && !message.isBlank()) {
                    notificationMsg += ": " + message;
                }
                notificationService.createNotification(
                        conn, sharer,
                        NotificationType.POST_SHARED,
                        notificationMsg,
                        "/post/" + postId);
                sharedCount++;
            }
        }
        logger.info("Post id: {} shared to {} users by: {}", postId, sharedCount, sharer.getUsername());
    }

    @Transactional(readOnly = true)
    public List<SharedPost> getSharedPostsForUser(User user) {
        logger.debug("Fetching shared posts for user: {}", user.getUsername());
        return sharedPostRepository.findByRecipientOrderBySharedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadShareCount(User user) {
        long count = sharedPostRepository.countByRecipientAndReadFalse(user);
        logger.debug("User: {} has {} unread shared posts", user.getUsername(), count);
        return count;
    }

    @Transactional(readOnly = true)
    public List<Post> searchByHashtag(String tag) {
        String cleanTag = tag.toLowerCase().replace("#", "");
        logger.info("Searching posts by hashtag: #{}", cleanTag);
        List<Post> results = postRepository.findByHashtag(cleanTag);
        logger.debug("Found {} posts for hashtag: #{}", results.size(), cleanTag);
        return results;
    }

    @Transactional(readOnly = true)
    public List<Post> getTrendingPosts() {
        logger.debug("Fetching top 10 trending posts");
        return postRepository.findTrendingPosts(PageRequest.of(0, 10));
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long postId) {
        logger.debug("Fetching comments for post id: {}", postId);
        Post post = getPostById(postId);
        return commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(post);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTrendingHashtags() {
        logger.debug("Computing trending hashtags");
        List<String> allHashtagStrings = postRepository.findAllHashtagStrings();
        Map<String, Long> counts = new HashMap<>();

        for (String hs : allHashtagStrings) {
            if (hs == null || hs.isBlank()) continue;
            for (String tag : hs.split(",")) {
                String t = tag.trim().toLowerCase();
                if (!t.isEmpty())
                    counts.merge(t, 1L, (a, b) -> a + b);
            }
        }

        if (counts.size() < 5) {
            logger.debug("Not enough hashtags found ({}), adding defaults", counts.size());
            counts.put("revconnect", 15L);
            counts.put("socialmedia", 12L);
            counts.put("saas", 10L);
            counts.put("innovation", 8L);
            counts.put("lavender", 7L);
            counts.put("premium", 6L);
            counts.put("tech", 5L);
            counts.put("design", 4L);
        }

        Map<String, Long> trending = counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        logger.info("Trending hashtags computed: {}", trending.keySet());
        return trending;
    }

    private String sanitizeHashtags(String hashtags) {
        if (hashtags == null || hashtags.isBlank()) return null;
        String sanitized = Arrays.stream(hashtags.split("[,\\s]+"))
                .map(t -> t.startsWith("#") ? t.substring(1) : t)
                .map(String::toLowerCase)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining(","));
        logger.debug("Sanitized hashtags: {}", sanitized);
        return sanitized;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPostAnalytics(Long postId, User currentUser) {
        logger.info("Fetching analytics for post id: {} by user: {}", postId, currentUser.getUsername());

        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser)) {
            logger.warn("Unauthorized analytics access on post id: {} by user: {}", postId, currentUser.getUsername());
            throw new IllegalArgumentException("Unauthorized");
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalLikes", post.getLikeCount());
        analytics.put("totalComments", post.getCommentCount());
        analytics.put("hashtags", post.getHashtagList());
        analytics.put("postType", post.getPostType());
        analytics.put("createdAt", post.getCreatedAt());

        logger.debug("Analytics for post id: {} - likes: {}, comments: {}", postId, post.getLikeCount(), post.getCommentCount());
        return analytics;
    }

    @Transactional(readOnly = true)
    public long getPostCountByUser(User user) {
        long count = postRepository.countByAuthorAndDeletedFalse(user);
        logger.debug("User: {} has {} posts", user.getUsername(), count);
        return count;
    }
}