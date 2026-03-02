package com.revconnect.controller.api;

import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.UserProfileResponse;
import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import com.revconnect.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse response = mapToUserProfileResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        UserProfileResponse response = mapToUserProfileResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);

        List<UserProfileResponse> responses = users.stream()
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Old password is incorrect"));
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @PutMapping("/privacy")
    public ResponseEntity<?> updatePrivacy(@RequestParam boolean isPrivate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsPrivate(isPrivate);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Privacy setting updated to: " + (isPrivate ? "Private" : "Public")));
    }

    private UserProfileResponse mapToUserProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setBio(user.getBio());
        response.setProfilePicture(user.getProfilePicture());
        response.setLocation(user.getLocation());
        response.setWebsite(user.getWebsite());
        response.setUserType(user.getUserType());
        response.setIsPrivate(user.getIsPrivate());
        response.setCreatedAt(user.getCreatedAt());

        // These will be implemented by other team members
        response.setFollowersCount(0L);
        response.setFollowingCount(0L);
        response.setPostsCount(0L);

        return response;
    }

    // Inner DTO classes
    public static class UpdateProfileRequest {
        private String fullName;
        private String bio;
        private String location;
        private String website;

        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getters and Setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}