package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String notifications(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("notifications", notificationService.getNotifications(currentUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        notificationService.markAllAsRead(currentUser);
        return "notifications/index";
    }

    @PostMapping("/mark-read/{id}")
    public String markRead(@PathVariable(name = "id") Long id,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes ra) {
        notificationService.markAsRead(id, currentUser);
        return "redirect:/notifications";
    }

    @PostMapping("/mark-all-read")
    public String markAllRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return "redirect:/notifications";
    }
}
