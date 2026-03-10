package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public String openChat(@PathVariable Long userId,
                           @AuthenticationPrincipal User currentUser,
                           Model model,
                           RedirectAttributes ra) {
        try {
            User target = userService.findById(userId);
            if (target.equals(currentUser)) {
                return "redirect:/profile/" + currentUser.getUsername();
            }
            model.addAttribute("targetUser", target);
            model.addAttribute("currentUser", currentUser);
            // This is a placeholder for the actual messaging view.
            return "messages/chat";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to open chat.");
            return "redirect:/feed";
        }
    }
}
