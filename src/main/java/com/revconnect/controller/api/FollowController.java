package com.revconnect.controller.api;

import com.revconnect.dto.request.UserSearchRequest;
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.PageResponse;
import com.revconnect.service.ProfileService;
import com.revconnect.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FollowController {

    private final ProfileService profileService;
    private final SearchService searchService;

    public FollowController(ProfileService profileService, SearchService searchService) {
        this.profileService = profileService;
        this.searchService = searchService;
    }

    @PostMapping("/follow/{username}")
    public ResponseEntity<?> followUser(@PathVariable String username) {
        MessageResponse response = profileService.followUser(username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/follow/{username}")
    public ResponseEntity<?> unfollowUser(@PathVariable String username) {
        MessageResponse response = profileService.unfollowUser(username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/followers/{username}")
    public ResponseEntity<?> removeFollower(@PathVariable String username) {
        MessageResponse response = profileService.removeFollower(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/followers/{username}")
    public ResponseEntity<?> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserSearchRequest request = new UserSearchRequest();
        request.setPage(page);
        request.setSize(size);

        PageResponse<EnhancedProfileResponse> response = searchService.getFollowers(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/following/{username}")
    public ResponseEntity<?> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserSearchRequest request = new UserSearchRequest();
        request.setPage(page);
        request.setSize(size);

        PageResponse<EnhancedProfileResponse> response = searchService.getFollowing(username, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/follow/stats/{username}")
    public ResponseEntity<?> getFollowStats(@PathVariable String username) {
        long followers = profileService.getFollowersCount(username);
        long following = profileService.getFollowingCount(username);
        return ResponseEntity.ok(new FollowStats(followers, following));
    }

    static class FollowStats {
        private long followers;
        private long following;

        public FollowStats(long followers, long following) {
            this.followers = followers;
            this.following = following;
        }

        public long getFollowers() { return followers; }
        public long getFollowing() { return following; }
    }
}