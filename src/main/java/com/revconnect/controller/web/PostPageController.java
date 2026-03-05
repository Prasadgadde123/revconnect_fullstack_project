// src/main/java/com/revconnect/controller/web/PostPageController.java
package com.revconnect.controller.web;

import com.revconnect.dto.response.PostResponse;
import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import com.revconnect.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class PostPageController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostPageController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    // ─── Guard helper ────────────────────────────────────────────────────────

    private boolean notLoggedIn(HttpSession session) {
        return session.getAttribute("user") == null;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    /** Show the create-post form */
    @GetMapping("/posts/create")
    public String showCreateForm(HttpSession session, Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        model.addAttribute("title", "Create Post");
        return "post/create";
    }

    /** Handle form submission from post/create.html */
    @PostMapping("/posts/create")
    public String handleCreatePost(
            @RequestParam String content,
            @RequestParam(required = false) String hashtags,
            HttpSession session,
            RedirectAttributes ra) {

        if (notLoggedIn(session)) return "redirect:/login";

        try {
            com.revconnect.dto.request.CreatePostRequest req =
                    new com.revconnect.dto.request.CreatePostRequest();
            req.setContent(content);
            req.setHashtags(hashtags);
            postService.createPost(req);
            ra.addFlashAttribute("success", "Post created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to create post: " + e.getMessage());
        }
        return "redirect:/feed";
    }

    // ─── VIEW SINGLE POST ────────────────────────────────────────────────────

    @GetMapping("/posts/{postId}")
    public String viewPost(@PathVariable Long postId,
                           HttpSession session,
                           Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        try {
            PostResponse post = postService.getPostById(postId);
            model.addAttribute("post", post);
            model.addAttribute("title", "Post by @" + post.getAuthorUsername());
        } catch (Exception e) {
            return "redirect:/feed?error=postnotfound";
        }
        return "post/view";
    }

    // ─── EDIT ────────────────────────────────────────────────────────────────

    /** Show the edit-post form */
    @GetMapping("/posts/{postId}/edit")
    public String showEditForm(@PathVariable Long postId,
                               HttpSession session,
                               Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        User sessionUser = (User) session.getAttribute("user");
        try {
            PostResponse post = postService.getPostById(postId);
            // Only the author can access the edit form
            if (!post.getAuthorId().equals(sessionUser.getId())) {
                return "redirect:/feed?error=unauthorized";
            }
            model.addAttribute("post", post);
            model.addAttribute("title", "Edit Post");
        } catch (Exception e) {
            return "redirect:/feed?error=postnotfound";
        }
        return "post/edit";
    }

    /** Handle edit form submission */
    @PostMapping("/posts/{postId}/edit")
    public String handleEditPost(@PathVariable Long postId,
                                 @RequestParam String content,
                                 @RequestParam(required = false) String hashtags,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        try {
            com.revconnect.dto.request.UpdatePostRequest req =
                    new com.revconnect.dto.request.UpdatePostRequest();
            req.setContent(content);
            req.setHashtags(hashtags);
            postService.updatePost(postId, req);
            ra.addFlashAttribute("success", "Post updated successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update post: " + e.getMessage());
        }
        return "redirect:/posts/" + postId;
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    /** Delete via POST (HTML forms don't support DELETE) */
    @PostMapping("/posts/{postId}/delete")
    public String handleDeletePost(@PathVariable Long postId,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        try {
            postService.deletePost(postId);
            ra.addFlashAttribute("success", "Post deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete post: " + e.getMessage());
        }
        return "redirect:/feed";
    }

    // ─── REPOST ──────────────────────────────────────────────────────────────

    @PostMapping("/posts/{postId}/repost")
    public String handleRepost(@PathVariable Long postId,
                               @RequestParam(required = false) String comment,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (notLoggedIn(session)) return "redirect:/login";
        try {
            postService.repostPost(postId, comment);
            ra.addFlashAttribute("success", "Post reposted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to repost: " + e.getMessage());
        }
        return "redirect:/feed";
    }

    // ─── FEED PAGE ───────────────────────────────────────────────────────────

    /**
     * Overrides the stub in PageController — renders real feed with posts.
     * NOTE: Add this in PageController instead if you prefer one web controller.
     *       Here it's separate to keep post logic isolated.
     */
    @GetMapping("/feed")
    public String feed(@RequestParam(defaultValue = "0")  int page,
                       @RequestParam(defaultValue = "10") int size,
                       HttpSession session,
                       Model model) {
        if (notLoggedIn(session)) return "redirect:/login";

        User user = (User) session.getAttribute("user");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("title", "Feed");

        try {
            Page<PostResponse> feedPage = postService.getFeedPosts(page, size);
            model.addAttribute("posts",        feedPage.getContent());
            model.addAttribute("currentPage",  feedPage.getNumber());
            model.addAttribute("totalPages",   feedPage.getTotalPages());
            model.addAttribute("hasNext",      feedPage.hasNext());
            model.addAttribute("hasPrev",      feedPage.hasPrevious());
            model.addAttribute("postsCount",   postService.countPostsByUser(user.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("posts", java.util.Collections.emptyList());
            model.addAttribute("feedError", "Could not load feed: " + e.getMessage());
        }

        return "feed/index";
    }

    // ─── HASHTAG SEARCH ──────────────────────────────────────────────────────

    @GetMapping("/posts/hashtag")
    public String searchByHashtag(@RequestParam String tag,
                                  @RequestParam(defaultValue = "0")  int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session,
                                  Model model) {
        if (notLoggedIn(session)) return "redirect:/login";
        try {
            Page<PostResponse> results = postService.searchByHashtag(tag, page, size);
            model.addAttribute("posts",         results.getContent());
            model.addAttribute("hashtag",       tag);
            model.addAttribute("currentPage",   results.getNumber());
            model.addAttribute("totalPages",    results.getTotalPages());
            model.addAttribute("totalElements", results.getTotalElements());
        } catch (Exception e) {
            model.addAttribute("error", "Search failed: " + e.getMessage());
        }
        return "post/hashtag-results";
    }
}