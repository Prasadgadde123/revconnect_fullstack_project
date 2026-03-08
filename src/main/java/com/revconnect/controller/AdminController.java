package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.PostService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        return "admin/dashboard";
    }

    @PostMapping("/users/{userId}/toggle")
    public String toggleUser(@PathVariable Long userId, RedirectAttributes ra) {
        userService.toggleUserEnabled(userId);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin";
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId, RedirectAttributes ra) {
        postService.adminDeletePost(postId);
        ra.addFlashAttribute("success", "Post deleted.");
        return "redirect:/admin";
    }
}
