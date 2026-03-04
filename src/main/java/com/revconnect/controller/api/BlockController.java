// src/main/java/com/revconnect/controller/api/BlockController.java
package com.revconnect.controller.api;

import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.User;
import com.revconnect.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BlockController {

    private final ProfileService profileService;

    public BlockController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<?> blockUser(@PathVariable String username) {
        MessageResponse response = profileService.blockUser(username);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> unblockUser(@PathVariable String username) {
        MessageResponse response = profileService.unblockUser(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getBlockedUsers() {
        List<User> blockedUsers = profileService.getBlockedUsers();
        return ResponseEntity.ok(blockedUsers);
    }

    @GetMapping("/check/{username}")
    public ResponseEntity<?> isBlocked(@PathVariable String username) {
        boolean blocked = profileService.isUserBlocked(username);
        return ResponseEntity.ok(new BlockStatus(blocked));
    }

    static class BlockStatus {
        private boolean blocked;

        public BlockStatus(boolean blocked) {
            this.blocked = blocked;
        }

        public boolean isBlocked() { return blocked; }
    }
}