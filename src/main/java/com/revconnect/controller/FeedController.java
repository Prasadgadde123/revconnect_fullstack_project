package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.NotificationService;
import com.revconnect.service.PostService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.revconnect.entity.Post;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping({"/", "/feed"})
    public String feed(@AuthenticationPrincipal User currentUser,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String filterType,
                       @RequestParam(required = false) String filterRole,
                       Model model) {
        Page<Post> feedPage = postService.getFeedPosts(currentUser, page, filterType, filterRole);
        model.addAttribute("posts", feedPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedPage.getTotalPages());
        model.addAttribute("trendingHashtags", postService.getTrendingHashtags());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        model.addAttribute("suggestedUsers", userService.searchUsers("").stream()
                .filter(u -> !u.equals(currentUser)).limit(5).toList());

        model.addAttribute("activeFilterType", filterType);
        model.addAttribute("activeFilterRole", filterRole);

        if (currentUser.getRole() == com.revconnect.enums.UserRole.BUSINESS || currentUser.getRole() == com.revconnect.enums.UserRole.CREATOR) {
            model.addAttribute("scheduledPosts", postService.getScheduledPosts(currentUser));
            model.addAttribute("soonToBePublishedPosts", postService.getSoonToBePublishedPosts(currentUser));
        }

        return "feed/index";
    }

    @GetMapping("/explore")
    public String explore(@AuthenticationPrincipal User currentUser,
                          @RequestParam(required = false) String hashtag,
                          @RequestParam(required = false) String search,
                          Model model) {
        model.addAttribute("trendingPosts", postService.getTrendingPosts());
        model.addAttribute("trendingHashtags", postService.getTrendingHashtags());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        if (hashtag != null) {
            model.addAttribute("hashtagPosts", postService.searchByHashtag(hashtag));
            model.addAttribute("activeHashtag", hashtag);
        }
        if (search != null) {
            model.addAttribute("searchResults", userService.searchUsers(search));
            model.addAttribute("searchQuery", search);
        }
        return "feed/explore";
    }
}
