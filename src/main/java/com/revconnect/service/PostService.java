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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final SharedPostRepository sharedPostRepository;
    private final NotificationService notificationService;
    private final ConnectionService connectionService;
    private final UserService userService;

    public Post createPost(User author, PostCreateDTO dto) {
        Post post = Post.builder()
                .content(dto.getContent())
                .author(author)
                .hashtags(sanitizeHashtags(dto.getHashtags()))
                .postType(dto.getPostType() != null ? dto.getPostType() : PostType.REGULAR)
                .ctaButtonText(dto.getCtaButtonText())
                .ctaButtonUrl(dto.getCtaButtonUrl())
                .taggedProducts(parseTaggedProducts(dto.getTaggedProducts()))
                .scheduledAt(dto.getScheduledAt())
                .build();
        Post saved = postRepository.save(post);
        log.info("Post created by {}", author.getUsername());
        return saved;
    }

    public Post updatePost(Long postId, User currentUser, PostCreateDTO dto) {
        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser))
            throw new IllegalArgumentException("Unauthorized");
        post.setContent(dto.getContent());
        post.setHashtags(sanitizeHashtags(dto.getHashtags()));
        if (dto.getCtaButtonText() != null)
            post.setCtaButtonText(dto.getCtaButtonText());
        if (dto.getCtaButtonUrl() != null)
            post.setCtaButtonUrl(dto.getCtaButtonUrl());
        if (dto.getTaggedProducts() != null)
            post.setTaggedProducts(parseTaggedProducts(dto.getTaggedProducts()));
        post.setScheduledAt(dto.getScheduledAt());
        return postRepository.save(post);
    }

    private List<String> parseTaggedProducts(String taggedProductsStr) {
        if (taggedProductsStr == null || taggedProductsStr.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(taggedProductsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId, User currentUser) {
        Post post = getPostById(postId);
        if (!post.getAuthor().getId().equals(currentUser.getId()) && currentUser.getRole() != com.revconnect.enums.UserRole.ADMIN) {
            throw new IllegalArgumentException("Unauthorized");
        }
        post.setDeleted(true);
        postRepository.save(post);
    }

    public void adminDeletePost(Long postId) {
        Post post = getPostById(postId);
        post.setDeleted(true);
        postRepository.save(post);
    }

    public Post toggleLike(Long postId, User user) {
        Post post = getPostById(postId);
        if (post.getLikes().contains(user)) {
            post.getLikes().remove(user);
        } else {
            post.getLikes().add(user);
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
        Post post = getPostById(postId);
        Comment comment = Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .build();
        Comment saved = commentRepository.save(comment);

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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthor().equals(currentUser))
            throw new IllegalArgumentException("Unauthorized");
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public Post repost(Long originalPostId, User author) {
        Post original = getPostById(originalPostId);

        // Check if user already reposted this post
        List<Post> existingReposts = postRepository.findRepostsByOriginalPostAndAuthor(original, author);
        if (!existingReposts.isEmpty()) {
            // Soft-delete all reposts by this user (avoids FK constraint issues)
            for (Post r : existingReposts) {
                r.setDeleted(true);
            }
            postRepository.saveAll(existingReposts);
            return null; // indicates undo
        }

        // Create new repost
        Post repost = Post.builder()
                .content(original.getContent())
                .author(author)
                .postType(PostType.REPOST)
                .originalPost(original)
                .hashtags(original.getHashtags())
                .build();
        Post saved = postRepository.save(repost);

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
        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser))
            throw new IllegalArgumentException("Unauthorized");

        boolean newPinnedStatus = !post.isPinned();

        // If we are pinning this post, we must unpin all other posts first
        if (newPinnedStatus) {
            List<Post> pinnedPosts = postRepository.findPinnedPosts(currentUser);
            for (Post p : pinnedPosts) {
                p.setPinned(false);
            }
            postRepository.saveAll(pinnedPosts);
        }

        post.setPinned(newPinnedStatus);
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    @Transactional(readOnly = true)
    public List<Post> getUserPosts(User user) {
        return postRepository.findPublishedUserPosts(user, java.time.LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Post> getPublishedUserPosts(User user) {
        return postRepository.findPublishedUserPosts(user, java.time.LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Post> getUserReposts(User user) {
        return postRepository.findRepostsByAuthor(user);
    }

    @Transactional(readOnly = true)
    public List<Post> getScheduledPosts(User author) {
        return postRepository.findScheduledPosts(author, java.time.LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Post> getSoonToBePublishedPosts(User author) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime cutoffTime = now.plusMinutes(5); // Increased to 5 minutes to be safe
        return postRepository.findSoonToBePublishedPosts(author, now, cutoffTime);
    }

    @Transactional(readOnly = true)
    public Page<Post> getFeedPosts(User currentUser, int page, String type, String userRole) {
        // Refresh currentUser to avoid LazyInitializationException with following
        // collection
        currentUser = userService.findByUsername(currentUser.getUsername());

        List<User> connections = connectionService.getConnections(currentUser);
        List<User> following = new ArrayList<>(currentUser.getFollowing());
        Set<User> feedUsers = new HashSet<>();
        feedUsers.addAll(connections);
        feedUsers.addAll(following);
        feedUsers.add(currentUser);

        List<Long> userIds = feedUsers.stream().map(User::getId).collect(java.util.stream.Collectors.toList());

        PageRequest pageable = PageRequest.of(page, 20);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (type != null && !type.isBlank()) {
            return postRepository.findFeedPostsByType(userIds,
                    com.revconnect.enums.PostType.valueOf(type.toUpperCase()), now, pageable);
        } else if (userRole != null && !userRole.isBlank()) {
            return postRepository.findFeedPostsByRole(userIds,
                    com.revconnect.enums.UserRole.valueOf(userRole.toUpperCase()), now, pageable);
        }
        return postRepository.findFeedPosts(userIds, now, pageable);
    }

    /**
     * Professional Ranked Feed: Fetches posts from the social circle
     * and ranks them by engagement score.
     */
    @Transactional(readOnly = true)
    public Page<Post> getPersonalizedFeed(User currentUser, int page) {
        currentUser = userService.findByUsername(currentUser.getUsername());

        List<User> connections = connectionService.getConnections(currentUser);
        List<User> following = new ArrayList<>(currentUser.getFollowing());
        Set<User> feedUsers = new HashSet<>();
        feedUsers.addAll(connections);
        feedUsers.addAll(following);
        feedUsers.add(currentUser);

        List<Long> userIds = feedUsers.stream().map(User::getId).collect(Collectors.toList());
        PageRequest pageable = PageRequest.of(page, 20);

        return postRepository.findRankedFeedPosts(userIds, java.time.LocalDateTime.now(), pageable);
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long postId) {
        return postRepository.countLikes(postId);
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Long postId) {
        return commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(getPostById(postId)).size();
    }

    public void shareToSelectedUsers(Long postId, User sharer, List<Long> userIds, String message) {
        Post post = getPostById(postId);
        List<User> connections = connectionService.getConnections(sharer);

        for (User conn : connections) {
            if (userIds.contains(conn.getId())) {
                // Save the SharedPost record
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
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SharedPost> getSharedPostsForUser(User user) {
        return sharedPostRepository.findByRecipientOrderBySharedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public long getUnreadShareCount(User user) {
        return sharedPostRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional(readOnly = true)
    public List<Post> searchByHashtag(String tag) {
        return postRepository.findByHashtag(tag.toLowerCase().replace("#", ""));
    }

    @Transactional(readOnly = true)
    public List<Post> getTrendingPosts() {
        return postRepository.findTrendingPosts(PageRequest.of(0, 10));
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long postId) {
        Post post = getPostById(postId);
        return commentRepository.findByPostAndDeletedFalseOrderByCreatedAtAsc(post);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTrendingHashtags() {
        List<String> allHashtagStrings = postRepository.findAllHashtagStrings();
        Map<String, Long> counts = new HashMap<>();
        for (String hs : allHashtagStrings) {
            if (hs == null || hs.isBlank())
                continue;
            for (String tag : hs.split(",")) {
                String t = tag.trim().toLowerCase();
                if (!t.isEmpty())
                    counts.merge(t, 1L, (a, b) -> a + b);
            }
        }

        // Add default hashtags if none exist to populate the UI
        if (counts.size() < 5) {
            counts.put("revconnect", 15L);
            counts.put("socialmedia", 12L);
            counts.put("saas", 10L);
            counts.put("innovation", 8L);
            counts.put("lavender", 7L);
            counts.put("premium", 6L);
            counts.put("tech", 5L);
            counts.put("design", 4L);
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String sanitizeHashtags(String hashtags) {
        if (hashtags == null || hashtags.isBlank())
            return null;
        return Arrays.stream(hashtags.split("[,\\s]+"))
                .map(t -> t.startsWith("#") ? t.substring(1) : t)
                .map(String::toLowerCase)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining(","));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPostAnalytics(Long postId, User currentUser) {
        Post post = getPostById(postId);
        if (!post.getAuthor().equals(currentUser))
            throw new IllegalArgumentException("Unauthorized");
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalLikes", post.getLikeCount());
        analytics.put("totalComments", post.getCommentCount());
        analytics.put("hashtags", post.getHashtagList());
        analytics.put("postType", post.getPostType());
        analytics.put("createdAt", post.getCreatedAt());
        return analytics;
    }
    @Transactional(readOnly = true)
    public long getPostCountByUser(User user) {
        return postRepository.countByAuthorAndDeletedFalse(user);
    }
}
