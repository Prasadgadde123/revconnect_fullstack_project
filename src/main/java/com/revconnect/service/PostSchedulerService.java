package com.revconnect.service;

import com.revconnect.entity.Post;
import com.revconnect.enums.NotificationType;
import com.revconnect.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostSchedulerService {

    private final PostRepository postRepository;
    private final NotificationService notificationService;

    /**
     * Scan every minute for posts that are due to be published
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void publishDuePosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> duePosts = postRepository.findDuePosts(now);
        
        if (duePosts.isEmpty()) return;
        
        log.info("Found {} due posts to publish", duePosts.size());
        
        for (Post post : duePosts) {
            try {
                publishPost(post);
            } catch (Exception e) {
                log.error("Failed to publish post {}: {}", post.getId(), e.getMessage());
            }
        }
    }

    private void publishPost(Post post) {
        post.setPublished(true);
        postRepository.save(post);
        
        // Notify followers
        String authorName = post.getAuthor().getDisplayNameOrUsername();
        String message = String.format("New post from %s: %s", authorName, truncate(post.getContent(), 50));
        String linkUrl = "/post/" + post.getId();
        
        post.getAuthor().getFollowers().forEach(follower -> {
            notificationService.createNotification(
                follower, 
                post.getAuthor(), 
                NotificationType.SYSTEM, 
                message, 
                linkUrl
            );
        });
        
        log.info("Post {} by {} is now live!", post.getId(), post.getAuthor().getUsername());
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length) + "...";
    }
}
