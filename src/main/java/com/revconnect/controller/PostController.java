package com.revconnect.controller;

import com.revconnect.dto.PostCreateDTO;
import com.revconnect.entity.Post;
import com.revconnect.entity.User;
import com.revconnect.service.NotificationService;
import com.revconnect.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final NotificationService notificationService;
    private final com.revconnect.service.ConnectionService connectionService;

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("postDTO", new PostCreateDTO());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
        return "post/create";
    }

    @PostMapping("/create")
    public String createPost(@AuthenticationPrincipal User user,
                             @Valid @ModelAttribute("postDTO") PostCreateDTO dto,
                             BindingResult result,
                             RedirectAttributes ra,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
            return "post/create";
        }
        try {
            postService.createPost(user, dto);
            ra.addFlashAttribute("success", "Post created!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/feed";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id,
                           @AuthenticationPrincipal User currentUser,
                           Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", postService.getComments(id));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser));
        return "post/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Post post = postService.getPostById(id);
        if (!post.getAuthor().equals(user)) {
            return "redirect:/feed";
        }
        PostCreateDTO dto = PostCreateDTO.builder()
                .content(post.getContent())
                .hashtags(post.getHashtags())
                .postType(post.getPostType())
                .ctaButtonText(post.getCtaButtonText())
                .ctaButtonUrl(post.getCtaButtonUrl())
                // Assuming taggedProducts conversion to string if needed
                .taggedProducts(post.getTaggedProducts() != null ? String.join(", ", post.getTaggedProducts()) : "")
                .build();
        model.addAttribute("postDTO", dto);
        model.addAttribute("postId", id);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
        return "post/edit";
    }

    @PostMapping("/{id}/edit")
    public String editPost(@PathVariable Long id,
                           @AuthenticationPrincipal User user,
                           @Valid @ModelAttribute("postDTO") PostCreateDTO dto,
                           BindingResult result,
                           RedirectAttributes ra,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
            return "post/edit";
        }
        try {
            postService.updatePost(id, user, dto);
            ra.addFlashAttribute("success", "Post updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes ra) {
        try {
            postService.deletePost(id, user);
            ra.addFlashAttribute("success", "Post deleted");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/feed";
    }

    @PostMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id,
                                                          @AuthenticationPrincipal User user) {
        Post updatedPost = postService.toggleLike(id, user);
        boolean isLiked = updatedPost.getLikes().contains(user);
        long newCount = postService.getLikeCount(id);
        return ResponseEntity.ok(Map.of("liked", isLiked, "count", newCount));
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @AuthenticationPrincipal User user,
                             @RequestParam String content,
                             RedirectAttributes ra) {
        if (content.isBlank()) {
            ra.addFlashAttribute("error", "Comment cannot be empty");
            return "redirect:/post/" + id;
        }
        try {
            postService.addComment(id, user, content);
            ra.addFlashAttribute("success", "Comment added");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @AuthenticationPrincipal User user,
                                @RequestParam Long postId,
                                RedirectAttributes ra) {
        try {
            postService.deleteComment(commentId, user);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/post/" + postId;
    }

    @PostMapping("/{id}/repost")
    public String repost(@PathVariable Long id,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes ra) {
        try {
            Post result = postService.repost(id, user);
            if (result == null) {
                ra.addFlashAttribute("success", "Repost removed!");
            } else {
                ra.addFlashAttribute("success", "Post shared!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/feed";
    }

    @PostMapping("/{id}/pin")
    public String togglePin(@PathVariable Long id,
                            @AuthenticationPrincipal User user,
                            RedirectAttributes ra) {
        try {
            postService.togglePin(id, user);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/" + user.getUsername();
    }

    @GetMapping("/{id}/send")
    public String showSendPage(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        model.addAttribute("connections", connectionService.getConnections(user));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
        return "post/send";
    }

    @PostMapping("/{id}/send")
    public String sendToSelectedUsers(@PathVariable Long id,
                                      @AuthenticationPrincipal User user,
                                      @RequestParam(required = false) java.util.List<Long> userIds,
                                      @RequestParam(required = false) String message,
                                      RedirectAttributes ra) {
        if (userIds == null || userIds.isEmpty()) {
            ra.addFlashAttribute("error", "Please select at least one connection to send the post to");
            return "redirect:/post/" + id + "/send";
        }
        try {
            postService.shareToSelectedUsers(id, user, userIds, message);
            ra.addFlashAttribute("success", "Post sent successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/post/" + id;
    }

    @GetMapping("/scheduled")
    public String viewScheduledPosts(@AuthenticationPrincipal User user, Model model) {
        if (user.getRole() != com.revconnect.enums.UserRole.BUSINESS && user.getRole() != com.revconnect.enums.UserRole.CREATOR) {
            return "redirect:/feed";
        }
        model.addAttribute("scheduledPosts", postService.getScheduledPosts(user));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
        return "post/scheduled";
    }

    @GetMapping("/shared")
    public String viewSharedPosts(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("sharedPosts", postService.getSharedPostsForUser(user));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));
        return "post/shared";
    }
}
