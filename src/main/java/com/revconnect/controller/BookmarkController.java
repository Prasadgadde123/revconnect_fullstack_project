package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final UserService userService;

    @PostMapping("/toggle/{userId}")
    public String toggleBookmark(@PathVariable Long userId,
                                  @AuthenticationPrincipal User currentUser,
                                  RedirectAttributes ra) {
        try {
            User target = userService.findById(userId);
            userService.toggleBookmark(currentUser, target);
            ra.addFlashAttribute("success", "Bookmarks updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update bookmark.");
        }
        return "redirect:/profile/" + userService.findById(userId).getUsername();
    }
}
