package com.revconnect.service;

import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserService userService;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Map<String, Object> getUserAnalytics(User user) {
        Map<String, Object> analytics = new HashMap<>();

        // Follower demographics
        List<User> followers = userService.getFollowers(user);
        Map<String, Integer> roleBreakdown = new HashMap<>();
        for (UserRole role : UserRole.values()) {
            roleBreakdown.put(role.name(), 0);
        }
        for (User follower : followers) {
            String roleName = follower.getRole().name();
            roleBreakdown.put(roleName, roleBreakdown.getOrDefault(roleName, 0) + 1);
        }

        // Post metrics
        List<Post> posts = postService.getUserPosts(user);
        long totalLikes = 0;
        long totalComments = 0;
        for (Post post : posts) {
            totalLikes += postService.getLikeCount(post.getId());
            totalComments += postService.getCommentCount(post.getId());
        }

        analytics.put("totalFollowers", followers.size());
        analytics.put("roleBreakdown", roleBreakdown);
        analytics.put("totalPosts", posts.size());
        analytics.put("totalLikes", totalLikes);
        analytics.put("totalComments", totalComments);

        // Engagement rate
        double engagementRate = 0;
        if (followers.size() > 0 && posts.size() > 0) {
            engagementRate = ((double) (totalLikes + totalComments) / posts.size()) / followers.size() * 100;
        }
        analytics.put("engagementRate", String.format("%.2f", engagementRate));

        return analytics;
    }
}
