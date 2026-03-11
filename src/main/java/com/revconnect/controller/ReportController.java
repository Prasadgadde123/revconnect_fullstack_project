package com.revconnect.controller;

import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import com.revconnect.service.PostService;
import com.revconnect.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final PostService postService;

    @PostMapping("/create")
    public String createReport(@AuthenticationPrincipal User currentUser,
            @RequestParam(name = "postId") Long postId,
            @RequestParam(name = "reason") String reason,
            RedirectAttributes ra) {
        try {
            Post post = postService.getPostById(postId);
            reportService.createReport(currentUser, post.getAuthor(), post, reason);
            ra.addFlashAttribute("success", "Post reported successfully. Our team will review it.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to report post: " + e.getMessage());
        }
        return "redirect:/feed";
    }
}
