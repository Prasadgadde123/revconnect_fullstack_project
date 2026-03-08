package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.enums.NotificationType;
import com.revconnect.service.NotificationService;
import com.revconnect.enums.NotificationType;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/{userId}")
    public String follow(@PathVariable Long userId,
                          @AuthenticationPrincipal User currentUser,
                          RedirectAttributes ra) {
        User target = userService.findById(userId);
        if (!userService.isFollowing(currentUser, target)) {
            userService.follow(currentUser, target);
            notificationService.createNotification(
                    target, currentUser,
                    NotificationType.NEW_FOLLOWER,
                    currentUser.getDisplayNameOrUsername() + " started following you",
                    "/profile/" + currentUser.getUsername()
            );
            ra.addFlashAttribute("success", "Following " + target.getDisplayNameOrUsername());
        }
        return "redirect:/profile/" + target.getUsername();
    }

    @PostMapping("/unfollow/{userId}")
    public String unfollow(@PathVariable Long userId,
                            @AuthenticationPrincipal User currentUser,
                            RedirectAttributes ra) {
        User target = userService.findById(userId);
        userService.unfollow(currentUser, target);
        ra.addFlashAttribute("success", "Unfollowed " + target.getDisplayNameOrUsername());
        return "redirect:/profile/" + target.getUsername();
    }
}
