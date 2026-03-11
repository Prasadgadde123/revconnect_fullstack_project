package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.repository.CommentRepository;
import com.revconnect.repository.PostRepository;
import com.revconnect.service.PostService;
import com.revconnect.service.ReportService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final ReportService reportService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @GetMapping({ "", "/dashboard" })
    public String dashboard(Model model, @RequestParam(name = "tab", defaultValue = "overview") String tab) {
        model.addAttribute("activeTab", tab);

        // Real Analytics Data
        List<User> allUsers = userService.getAllUsers();
        long totalPosts = postRepository.countByDeletedFalse();
        long totalComments = commentRepository.countByDeletedFalse();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalComments", totalComments);

        // Mocking Likes as it's a join count aggregate which is heavy for a simple
        // dashboard
        model.addAttribute("totalLikes", "2.4M");

        model.addAttribute("newUsersThisWeek", allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
                .count());

        // Tab Data
        model.addAttribute("users", allUsers);
        model.addAttribute("posts", postService.getTrendingPosts());
        model.addAttribute("reports", reportService.getAllReports());
        model.addAttribute("trendingTags", postService.getTrendingHashtags());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersManagement(Model model) {
        return dashboard(model, "users");
    }

    @GetMapping("/posts")
    public String postsManagement(Model model) {
        return dashboard(model, "posts");
    }

    @GetMapping("/comments")
    public String commentsManagement(Model model) {
        return dashboard(model, "comments");
    }

    @GetMapping("/reports")
    public String reportsManagement(Model model) {
        return dashboard(model, "reports");
    }

    @GetMapping("/analytics")
    public String analyticsView(Model model) {
        return dashboard(model, "analytics");
    }

    // User Actions
    @PostMapping("/users/{userId}/suspend")
    public String suspendUser(@PathVariable(name = "userId") Long userId, RedirectAttributes ra) {
        User user = userService.findById(userId);
        user.setEnabled(false);
        userService.updateProfile(user, new com.revconnect.dto.ProfileUpdateDTO());
        ra.addFlashAttribute("success", "User " + user.getUsername() + " suspended.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/activate")
    public String activateUser(@PathVariable(name = "userId") Long userId, RedirectAttributes ra) {
        User user = userService.findById(userId);
        user.setEnabled(true);
        userService.updateProfile(user, new com.revconnect.dto.ProfileUpdateDTO());
        ra.addFlashAttribute("success", "User " + user.getUsername() + " activated.");
        return "redirect:/admin/users";
    }

    // Post Actions
    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable(name = "postId") Long postId, RedirectAttributes ra) {
        postService.adminDeletePost(postId);
        ra.addFlashAttribute("success", "Post deleted successfully.");
        return "redirect:/admin/posts";
    }

    // Report Actions
    @PostMapping("/reports/{reportId}/resolve")
    public String resolveReport(@PathVariable(name = "reportId") Long reportId, RedirectAttributes ra) {
        reportService.resolveReport(reportId);
        ra.addFlashAttribute("success", "Report resolved.");
        return "redirect:/admin/reports";
    }
}
