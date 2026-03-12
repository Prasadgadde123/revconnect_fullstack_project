package com.revconnect.controller;

import com.revconnect.dto.ReportResponseDTO;
import com.revconnect.entity.User;
import com.revconnect.service.PostService;
import com.revconnect.service.UserService;
import com.revconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final AdminService adminService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getPlatformStats());
        model.addAttribute("pendingReportsCount", adminService.getPendingReports().size());
        List<ReportResponseDTO> recentReports = adminService.getRecentReports();
        model.addAttribute("recentReports", recentReports);
        model.addAttribute("signupBlocked", adminService.getBooleanSetting("BLOCK_SIGNUP", false));
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(@RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("users", userService.adminSearchUsers(search));
            model.addAttribute("searchQuery", search);
        } else if (role != null && !role.isBlank()) {
            try {
                com.revconnect.enums.UserRole userRole = com.revconnect.enums.UserRole.valueOf(role.toUpperCase());
                model.addAttribute("users", userService.getUsersByRole(userRole));
                model.addAttribute("activeRole", role.toUpperCase());
            } catch (IllegalArgumentException e) {
                model.addAttribute("users", userService.getAllUsers());
            }
        } else {
            model.addAttribute("users", userService.getAllUsers());
        }
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        return "admin/users";
    }

    @GetMapping("/content")
    public String contentModeration(Model model) {
        model.addAttribute("posts", postService.getTrendingPosts()); // Show trending as default moderate-able content
        return "admin/content";
    }

    @GetMapping("/reports")
    public String reportsQueue(Model model) {
        List<ReportResponseDTO> reports = adminService.getAllReports();
        model.addAttribute("reports", reports);
        return "admin/reports";
    }

    @GetMapping("/settings")
    public String systemSettings(Model model) {
        model.addAttribute("signupBlocked", adminService.getBooleanSetting("BLOCK_SIGNUP", false));
        model.addAttribute("maintenanceMode", adminService.getBooleanSetting("MAINTENANCE_MODE", false));
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSetting(@RequestParam String key, @RequestParam String value, RedirectAttributes ra) {
        adminService.updateSetting(key, value);
        ra.addFlashAttribute("success", "System setting updated.");
        return "redirect:/admin/settings";
    }

    @PostMapping("/users/{userId}/toggle")
    public String toggleUser(@PathVariable Long userId, RedirectAttributes ra) {
        userService.toggleUserEnabled(userId);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes ra) {
        userService.deleteUser(userId);
        ra.addFlashAttribute("success", "User deleted successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/edit")
    public String editUser(@PathVariable Long userId,
            @RequestParam String displayName,
            @RequestParam String email,
            @RequestParam(defaultValue = "false") boolean enabled,
            RedirectAttributes ra) {
        userService.adminUpdateUser(userId, displayName, email, enabled);
        ra.addFlashAttribute("success", "User details updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId, RedirectAttributes ra) {
        postService.adminDeletePost(postId);
        ra.addFlashAttribute("success", "Post deleted.");
        return "redirect:/admin/content";
    }

    @PostMapping("/announcement")
    public String broadcast(@RequestParam String message, RedirectAttributes ra) {
        adminService.broadcastAnnouncement(message);
        ra.addFlashAttribute("success", "Announcement broadcasted to all users.");
        return "redirect:/admin/settings";
    }

    @PostMapping("/reports/{reportId}/resolve")
    public String resolveReport(@PathVariable Long reportId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal User admin,
            RedirectAttributes ra) {
        adminService.resolveReport(reportId, admin, note);
        ra.addFlashAttribute("success", "Report resolved.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{reportId}/dismiss")
    public String dismissReport(@PathVariable Long reportId,
            @AuthenticationPrincipal User admin,
            RedirectAttributes ra) {
        adminService.dismissReport(reportId, admin);
        ra.addFlashAttribute("success", "Report dismissed.");
        return "redirect:/admin/reports";
    }
}
