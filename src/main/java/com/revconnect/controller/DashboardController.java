package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.entity.Post;
import com.revconnect.entity.Notification;
import com.revconnect.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public String viewDashboard(@AuthenticationPrincipal User currentUser, Model model) {
        if (currentUser == null) return "redirect:/login";
        if (currentUser.getRole() == com.revconnect.enums.UserRole.ADMIN) return "redirect:/admin";
        if (currentUser.getRole() == com.revconnect.enums.UserRole.PERSONAL) return "redirect:/feed";

        // Refresh user to get latest collections
        User freshUser = userService.findById(currentUser.getId());
        
        // 1. Stats & Analytics
        Map<String, Object> stats = analyticsService.getUserAnalytics(freshUser);
        model.addAttribute("stats", stats);
        
        // 2. Recent Posts (authored by user)
        List<Post> recentPosts = postService.getUserPosts(freshUser).stream()
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentPosts", recentPosts);
        
        // 3. Notifications Preview
        List<Notification> notifications = notificationService.getNotifications(freshUser);
        model.addAttribute("notifications", notifications != null ? notifications.stream()
                .limit(6)
                .collect(Collectors.toList()) : List.of());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(freshUser));
        
        // 4. Trending Hashtags
        Map<String, Long> trendingTags = postService.getTrendingHashtags();
        model.addAttribute("trendingTags", trendingTags != null ? trendingTags : Map.of());
        
        // 5. Suggested Connections (not following and not self)
        Set<User> following = freshUser.getFollowing() != null ? freshUser.getFollowing() : Set.of();
        List<User> allUsers = userService.getAllUsers();
        List<User> suggestions = (allUsers != null ? allUsers.stream() : java.util.stream.Stream.<User>empty())
                .filter(u -> u != null && !u.equals(freshUser) && !following.contains(u))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("suggestions", suggestions);

        model.addAttribute("user", freshUser);
        
        return "dashboard/index";
    }
}
