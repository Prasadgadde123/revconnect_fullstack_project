package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.enums.NotificationType;
import com.revconnect.service.NotificationService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/{userId}")
    @ResponseBody
    public java.util.Map<String, Object> follow(@PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        User target = userService.findById(userId);
        if (!userService.isFollowing(currentUser, target)) {
            userService.follow(currentUser, target);
            notificationService.createNotification(
                    target, currentUser,
                    NotificationType.NEW_FOLLOWER,
                    currentUser.getDisplayNameOrUsername() + " started following you",
                    "/profile/" + currentUser.getUsername());
        }
        return java.util.Map.of(
                "success", true,
                "followerCount", userService.getFollowers(target).size(),
                "followingCount", userService.getFollowing(target).size(),
                "isFollowing", true);
    }

    @PostMapping("/unfollow/{userId}")
    @ResponseBody
    public java.util.Map<String, Object> unfollow(@PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        User target = userService.findById(userId);
        userService.unfollow(currentUser, target);
        return java.util.Map.of(
                "success", true,
                "followerCount", userService.getFollowers(target).size(),
                "followingCount", userService.getFollowing(target).size(),
                "isFollowing", false);
    }
}
