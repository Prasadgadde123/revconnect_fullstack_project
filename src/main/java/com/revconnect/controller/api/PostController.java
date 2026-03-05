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

    /** Single post by id */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            PostResponse response = postService.getPostById(postId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Own posts (for profile page) */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PostResponse> posts = postService.getMyPosts(page, size);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Posts by a specific user (for viewing another user's profile) */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PostResponse> posts = postService.getPostsByUser(userId, page, size);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Personalized feed */
    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PostResponse> posts = postService.getFeedPosts(page, size);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Trending posts */
    @GetMapping("/trending")
    public ResponseEntity<?> getTrending(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PostResponse> posts = postService.getTrendingPosts(page, size);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /** Search by hashtag */
    @GetMapping("/search")
    public ResponseEntity<?> searchByHashtag(
            @RequestParam String hashtag,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PostResponse> posts = postService.searchByHashtag(hashtag, page, size);
            return ResponseEntity.ok(posts);
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
            PostResponse response = postService.updatePost(postId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to update post: " + e.getMessage()));
        }
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            MessageResponse response = postService.deletePost(postId);
            return ResponseEntity.ok(response);
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
            PostResponse response = postService.repostPost(postId, comment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to repost: " + e.getMessage()));
        }
    }
}