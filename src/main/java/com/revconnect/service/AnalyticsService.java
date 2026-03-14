package com.revconnect.service;

import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final Logger logger = LogManager.getLogger(AnalyticsService.class);

    private final UserService userService;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Map<String, Object> getUserAnalytics(User user) {
        logger.info("Computing analytics for user: {}", user.getUsername());

        // Follower demographics
        List<User> followers = userService.getFollowers(user);
        logger.debug("User: {} has {} followers", user.getUsername(), followers.size());

        Map<String, Integer> roleBreakdown = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            roleBreakdown.put(role.name(), 0);
        }
        for (User follower : followers) {
            String roleName = follower.getRole().name();
            roleBreakdown.put(roleName, roleBreakdown.getOrDefault(roleName, 0) + 1);
        }
        logger.debug("Follower role breakdown for user: {} -> {}", user.getUsername(), roleBreakdown);

        // Post metrics
        List<Post> posts = postService.getUserPosts(user);
        logger.debug("User: {} has {} posts to analyze", user.getUsername(), posts.size());

        long totalLikes = 0;
        long totalComments = 0;
        for (Post post : posts) {
            totalLikes += postService.getLikeCount(post.getId());
            totalComments += postService.getCommentCount(post.getId());
        }
        logger.debug("User: {} total likes: {}, total comments: {}", user.getUsername(), totalLikes, totalComments);

        // Engagement rate
        double engagementRate = 0;
        if (followers.size() > 0 && posts.size() > 0) {
            engagementRate = ((double) (totalLikes + totalComments) / posts.size()) / followers.size() * 100;
        }

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalFollowers", followers.size());
        analytics.put("roleBreakdown", roleBreakdown);
        analytics.put("totalPosts", posts.size());
        analytics.put("totalLikes", totalLikes);
        analytics.put("totalComments", totalComments);
        analytics.put("engagementRate", String.format("%.2f", engagementRate));

        logger.info("Analytics computed for user: {} - followers: {}, posts: {}, engagementRate: {}%",
                user.getUsername(), followers.size(), posts.size(), String.format("%.2f", engagementRate));

        return analytics;
    }
}