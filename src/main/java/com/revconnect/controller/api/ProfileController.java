// src/main/java/com/revconnect/controller/api/ProfileController.java
package com.revconnect.controller.api;

import com.revconnect.dto.request.PrivacySettingsRequest;
import com.revconnect.dto.request.UpdateEnhancedProfileRequest;
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.service.FileStorageService;
import com.revconnect.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    private final ProfileService profileService;
    private final FileStorageService fileStorageService;

    public ProfileController(ProfileService profileService,
                             FileStorageService fileStorageService) {
        this.profileService = profileService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        EnhancedProfileResponse profile = profileService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username) {
        EnhancedProfileResponse profile = profileService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateEnhancedProfileRequest request) {
        MessageResponse response = profileService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/privacy")
    public ResponseEntity<?> updatePrivacy(@Valid @RequestBody PrivacySettingsRequest request) {
        MessageResponse response = profileService.updatePrivacySettings(request);
        return ResponseEntity.ok(response);
    }

    // ✅ FIXED: Actually saves the uploaded file to disk
    @PostMapping("/picture")
    public ResponseEntity<?> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            // Get current profile picture URL to delete old one
            EnhancedProfileResponse currentProfile = profileService.getMyProfile();
            String oldPictureUrl = currentProfile.getProfilePicture();

            // Save new file to disk → returns public URL like /uploads/profile-pictures/abc.jpg
            String newPictureUrl = fileStorageService.saveProfilePicture(file);

            // Save the URL to the database
            MessageResponse response = profileService.updateProfilePicture(newPictureUrl);

            // Delete old file from disk (if it was a local file)
            fileStorageService.deleteOldPicture(oldPictureUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to upload picture: " + e.getMessage()));
        }
    }

    // ✅ FIXED: Actually saves the uploaded cover photo to disk
    @PostMapping("/cover")
    public ResponseEntity<?> updateCoverPhoto(@RequestParam("file") MultipartFile file) {
        try {
            EnhancedProfileResponse currentProfile = profileService.getMyProfile();
            String oldCoverUrl = currentProfile.getCoverPhoto();

            String newCoverUrl = fileStorageService.saveProfilePicture(file); // reuses same storage
            MessageResponse response = profileService.updateCoverPhoto(newCoverUrl);

            fileStorageService.deleteOldPicture(oldCoverUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to upload cover photo: " + e.getMessage()));
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(@RequestBody DeactivateRequest request) {
        try {
            MessageResponse response = profileService.deactivateAccount(request.getReason());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to deactivate: " + e.getMessage()));
        }
    }

    public static class DeactivateRequest {
        private String password;
        private String reason;

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}