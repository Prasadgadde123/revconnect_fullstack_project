// src/main/java/com/revconnect/controller/api/PostController.java
package com.revconnect.controller.api;

import com.revconnect.dto.request.CreatePostRequest;
import com.revconnect.dto.request.UpdatePostRequest;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.PostResponse;
import com.revconnect.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request) {
        try {
            PostResponse response = postService.createPost(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to create post: " + e.getMessage()));
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.getPostById(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(postService.getMyPosts(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(postService.getPostsByUser(userId, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(postService.getFeedPosts(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<?> getTrending(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(postService.getTrendingPosts(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByHashtag(
            @RequestParam String hashtag,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(postService.searchByHashtag(hashtag, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        try {
            return ResponseEntity.ok(postService.updatePost(postId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to update post: " + e.getMessage()));
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.deletePost(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to delete post: " + e.getMessage()));
        }
    }

    // ─── REPOST ──────────────────────────────────────────────────────────────

    @PostMapping("/{postId}/repost")
    public ResponseEntity<?> repostPost(
            @PathVariable Long postId,
            @RequestParam(required = false) String comment) {
        try {
            return ResponseEntity.ok(postService.repostPost(postId, comment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to repost: " + e.getMessage()));
        }
    }

    // ─── FEATURE 3: PIN / UNPIN ──────────────────────────────────────────────

    /** Pin a post to the top of the current user's profile. */
    @PostMapping("/{postId}/pin")
    public ResponseEntity<?> pinPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.pinPost(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Unpin a previously pinned post. */
    @PostMapping("/{postId}/unpin")
    public ResponseEntity<?> unpinPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.unpinPost(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Get all pinned posts for a specific user's profile page. */
    @GetMapping("/user/{userId}/pinned")
    public ResponseEntity<?> getPinnedPosts(@PathVariable Long userId) {
        try {
            List<PostResponse> pinned = postService.getPinnedPosts(userId);
            return ResponseEntity.ok(pinned);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ─── FEATURE 2: SCHEDULED POSTS ──────────────────────────────────────────

    /** Get all scheduled posts for the current user (management view). */
    @GetMapping("/scheduled")
    public ResponseEntity<?> getScheduledPosts() {
        try {
            return ResponseEntity.ok(postService.getMyScheduledPosts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Cancel a scheduled post before it goes live. */
    @DeleteMapping("/{postId}/scheduled")
    public ResponseEntity<?> cancelScheduledPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.cancelScheduledPost(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    // ─── FEATURE 1: TAGGED PRODUCTS — helper for frontend dropdown ───────────

    /** Returns products/services the current user can tag in a post. */
    @GetMapping("/taggable-products")
    public ResponseEntity<?> getTaggableProducts() {
        try {
            return ResponseEntity.ok(postService.getMyTaggableProducts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}