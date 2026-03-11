package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.ConnectionService;
import com.revconnect.service.NotificationService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/request/{userId}")
    public String sendRequest(@PathVariable(name = "userId") Long userId,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes ra) {
        try {
            User target = userService.findById(userId);
            connectionService.sendRequest(currentUser, target);
            ra.addFlashAttribute("success", "Connection request sent!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/" + userService.findById(userId).getUsername();
    }

    @PostMapping("/accept/{connectionId}")
    public String acceptRequest(@PathVariable(name = "connectionId") Long connectionId,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes ra) {
        try {
            connectionService.acceptRequest(connectionId, currentUser);
            ra.addFlashAttribute("success", "Connection accepted!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/connections";
    }

    @PostMapping("/reject/{connectionId}")
    public String rejectRequest(@PathVariable(name = "connectionId") Long connectionId,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes ra) {
        try {
            connectionService.rejectRequest(connectionId, currentUser);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/connections";
    }

    @PostMapping("/remove/{userId}")
    public String removeConnection(@PathVariable(name = "userId") Long userId,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes ra) {
        User other = userService.findById(userId);
        connectionService.removeConnection(currentUser, other);
        ra.addFlashAttribute("success", "Connection removed.");
        return "redirect:/profile/connections";
    }
}
