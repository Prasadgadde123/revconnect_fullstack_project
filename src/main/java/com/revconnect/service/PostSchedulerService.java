package com.revconnect.service;

import com.revconnect.entity.Post;
import com.revconnect.enums.NotificationType;
import com.revconnect.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.revconnect.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSchedulerService {

    private static final Logger logger = LogManager.getLogger(PostSchedulerService.class);

    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishDuePosts() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Scheduler running at: {} - checking for due posts", now);

        List<Post> duePosts = postRepository.findDuePosts(now);

        if (duePosts.isEmpty()) {
            logger.debug("No due posts found at: {}", now);
            return;
        }

        logger.info("Found {} due posts to publish at: {}", duePosts.size(), now);

        for (Post post : duePosts) {
            try {
                publishPost(post);
            } catch (Exception e) {
                logger.error("Failed to publish post id: {} by user: {} - error: {}",
                        post.getId(), post.getAuthor().getUsername(), e.getMessage(), e);
            }
        }

        logger.info("Scheduler completed - processed {} posts", duePosts.size());
    }

    private void publishPost(Post post) {
        logger.info("Publishing scheduled post id: {} by user: {}", post.getId(), post.getAuthor().getUsername());

        post.setPublished(true);
        postRepository.save(post);

        String authorName = post.getAuthor().getDisplayNameOrUsername();
        String message = String.format("New post from %s: %s", authorName, truncate(post.getContent(), 50));
        String linkUrl = "/post/" + post.getId();

        int notifyCount = 0;
        for (User follower : post.getAuthor().getFollowers()) {
            notificationService.createNotification(
                    follower,
                    post.getAuthor(),
                    NotificationType.SYSTEM,
                    message,
                    linkUrl
            );
            notifyCount++;
        }

        logger.info("Post id: {} published and {} followers notified", post.getId(), notifyCount);
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length) + "...";
    }
}