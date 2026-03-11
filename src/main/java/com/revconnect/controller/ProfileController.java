package com.revconnect.controller;

import com.revconnect.dto.ProfileUpdateDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.ConnectionStatus;
import com.revconnect.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PostService postService;
    private final ConnectionService connectionService;
    private final NotificationService notificationService;
    private final AnalyticsService analyticsService;
    private final ProductService productService;

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username,
            @AuthenticationPrincipal User currentUser,
            Model model) {
        User profileUser = userService.findByUsername(username);
        boolean isSelf = profileUser.equals(currentUser);
        boolean connected = !isSelf && connectionService.areConnected(currentUser, profileUser);
        boolean pendingSent = !isSelf && connectionService.hasPendingRequest(currentUser, profileUser);
        boolean isFollowing = !isSelf && userService.isFollowing(currentUser, profileUser);
        boolean canViewContent = !profileUser.isPrivateProfile() || isSelf || connected;

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isSelf", isSelf);
        model.addAttribute("connected", connected);
        model.addAttribute("pendingSent", pendingSent);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isBookmarked",
                currentUser != null && !isSelf && userService.isBookmarked(currentUser, profileUser));
        model.addAttribute("canViewContent", canViewContent);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        model.addAttribute("followerCount", userService.getFollowers(profileUser).size());
        model.addAttribute("followingCount", userService.getFollowing(profileUser).size());
        model.addAttribute("connectionCount", connectionService.getConnections(profileUser).size());
        model.addAttribute("isProfilePage", true);

        if (canViewContent) {
            if (isSelf) {
                model.addAttribute("posts", postService.getUserPosts(profileUser));
            } else {
                model.addAttribute("posts", postService.getPublishedUserPosts(profileUser));
            }
            model.addAttribute("reposts", postService.getUserReposts(profileUser));
            if (profileUser.getRole() == com.revconnect.enums.UserRole.BUSINESS
                    || profileUser.getRole() == com.revconnect.enums.UserRole.CREATOR) {
                model.addAttribute("products", productService.getProductsByOwner(profileUser));
            }
        }
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal User currentUser, Model model) {
        ProfileUpdateDTO dto = ProfileUpdateDTO.builder()
                .displayName(currentUser.getDisplayName())
                .bio(currentUser.getBio())
                .location(currentUser.getLocation())
                .website(currentUser.getWebsite())
                .category(currentUser.getCategory())
                .businessAddress(currentUser.getBusinessAddress())
                .businessHours(currentUser.getBusinessHours())
                .contactEmail(currentUser.getContactEmail())
                .contactPhone(currentUser.getContactPhone())
                .privateProfile(currentUser.isPrivateProfile())
                .notifyConnectionRequests(currentUser.isNotifyConnectionRequests())
                .notifyLikes(currentUser.isNotifyLikes())
                .notifyComments(currentUser.isNotifyComments())
                .notifyFollowers(currentUser.isNotifyFollowers())
                .notifyShares(currentUser.isNotifyShares())
                .externalLinks(new java.util.ArrayList<>(currentUser.getExternalLinks()))
                .build();
        model.addAttribute("profileDTO", dto);
        model.addAttribute("user", currentUser);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
            @Valid @ModelAttribute("profileDTO") ProfileUpdateDTO dto,
            BindingResult result,
            RedirectAttributes ra,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", currentUser);
            model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
            return "profile/edit";
        }
        userService.updateProfile(currentUser, dto);
        ra.addFlashAttribute("success", "Profile updated!");
        return "redirect:/profile/" + currentUser.getUsername();
    }

    @PostMapping("/picture")
    public String uploadPicture(@AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a file");
            return "redirect:/profile/edit";
        }
        try {
            String uploadsDir = "uploads/profile-pics/";
            Files.createDirectories(Paths.get(uploadsDir));
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), Paths.get(uploadsDir + filename),
                    StandardCopyOption.REPLACE_EXISTING);
            userService.updateProfilePicture(currentUser, "/uploads/profile-pics/" + filename);
            ra.addFlashAttribute("success", "Profile picture updated!");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Failed to upload picture");
        }
        return "redirect:/profile/edit";
    }

    @GetMapping("/connections")
    public String myConnections(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("connections", connectionService.getConnections(currentUser));
        model.addAttribute("pendingReceived", connectionService.getPendingReceived(currentUser));
        model.addAttribute("pendingSent", connectionService.getPendingSent(currentUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "profile/connections";
    }

    @GetMapping("/followers")
    public String myFollowers(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("followers", userService.getFollowers(currentUser));
        model.addAttribute("following", userService.getFollowing(currentUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "profile/followers";
    }

    @GetMapping("/{username}/analytics")
    public String viewAnalytics(@PathVariable String username,
            @AuthenticationPrincipal User currentUser,
            Model model,
            RedirectAttributes ra) {
        User profileUser = userService.findByUsername(username);

        // Only let users view their own analytics
        if (!profileUser.equals(currentUser)) {
            ra.addFlashAttribute("error", "Unauthorized to view these analytics.");
            return "redirect:/profile/" + username;
        }

        // Only CREATOR or BUSINESS roles
        if (profileUser.getRole() == com.revconnect.enums.UserRole.PERSONAL
                || profileUser.getRole() == com.revconnect.enums.UserRole.ADMIN) {
            ra.addFlashAttribute("error", "Analytics are only available for Creator and Business accounts.");
            return "redirect:/profile/" + username;
        }

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("analytics", analyticsService.getUserAnalytics(profileUser));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));

        return "profile/analytics";
    }
}
