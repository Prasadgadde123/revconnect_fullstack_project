// src/main/java/com/revconnect/service/ProfileService.java
package com.revconnect.service;

import com.revconnect.dto.request.PrivacySettingsRequest;
import com.revconnect.dto.request.UpdateEnhancedProfileRequest;
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.Block;
import com.revconnect.model.user.Follow;
import com.revconnect.model.user.User;
import com.revconnect.repository.BlockRepository;
import com.revconnect.repository.FollowRepository;
import com.revconnect.repository.UserRepository;
import com.revconnect.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final PasswordEncoder passwordEncoder;  // ADD THIS

    public ProfileService(UserRepository userRepository,
                          FollowRepository followRepository,
                          BlockRepository blockRepository,
                          PasswordEncoder passwordEncoder) {  // ADD THIS
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
        this.passwordEncoder = passwordEncoder;  // ADD THIS
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    // ========== PROFILE CRUD OPERATIONS ==========

    @Transactional
    public EnhancedProfileResponse getProfile(String username) {
        User currentUser = getCurrentUser();
        User profileUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Check if profile is blocked
        if (isBlocked(currentUser, profileUser)) {
            throw new RuntimeException("This profile is not available");
        }

        // Increment profile views (only if not viewing own profile)
        if (!currentUser.getId().equals(profileUser.getId())) {
            profileUser.setProfileViews(profileUser.getProfileViews() + 1);
            userRepository.save(profileUser);
        }

        // Update last active
        currentUser.setLastActive(LocalDateTime.now());
        userRepository.save(currentUser);

        return mapToEnhancedProfileResponse(profileUser, currentUser);
    }

    @Transactional
    public EnhancedProfileResponse getMyProfile() {
        User currentUser = getCurrentUser();
        currentUser.setLastActive(LocalDateTime.now());
        userRepository.save(currentUser);
        return mapToEnhancedProfileResponse(currentUser, currentUser);
    }

    @Transactional
    public MessageResponse updateProfile(UpdateEnhancedProfileRequest request) {
        User currentUser = getCurrentUser();

        // Update basic info
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            currentUser.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            currentUser.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            currentUser.setWebsite(request.getWebsite());
        }

        // Update enhanced fields
        if (request.getOccupation() != null) {
            currentUser.setOccupation(request.getOccupation());
        }
        if (request.getCompany() != null) {
            currentUser.setCompany(request.getCompany());
        }
        if (request.getEducation() != null) {
            currentUser.setEducation(request.getEducation());
        }
        if (request.getSkills() != null) {
            currentUser.setSkills(request.getSkills());
        }
        if (request.getInterests() != null) {
            currentUser.setInterests(request.getInterests());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getTheme() != null) {
            currentUser.setTheme(request.getTheme());
        }

        // Update social links
        if (request.getFacebookUrl() != null) {
            currentUser.setFacebookUrl(request.getFacebookUrl());
        }
        if (request.getTwitterUrl() != null) {
            currentUser.setTwitterUrl(request.getTwitterUrl());
        }
        if (request.getInstagramUrl() != null) {
            currentUser.setInstagramUrl(request.getInstagramUrl());
        }
        if (request.getLinkedinUrl() != null) {
            currentUser.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            currentUser.setGithubUrl(request.getGithubUrl());
        }

        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);

        return new MessageResponse("Profile updated successfully");
    }

    @Transactional
    public MessageResponse updatePrivacySettings(PrivacySettingsRequest request) {
        User currentUser = getCurrentUser();

        if (request.getIsPrivate() != null) {
            currentUser.setIsPrivate(request.getIsPrivate());
        }
        if (request.getShowEmail() != null) {
            currentUser.setShowEmail(request.getShowEmail());
        }
        if (request.getShowPhone() != null) {
            currentUser.setShowPhone(request.getShowPhone());
        }
        if (request.getShowLocation() != null) {
            currentUser.setShowLocation(request.getShowLocation());
        }
        if (request.getShowLastActive() != null) {
            currentUser.setShowLastActive(request.getShowLastActive());
        }
        if (request.getAllowMessagesFromAnyone() != null) {
            currentUser.setAllowMessagesFromAnyone(request.getAllowMessagesFromAnyone());
        }
        if (request.getAllowTagging() != null) {
            currentUser.setAllowTagging(request.getAllowTagging());
        }

        userRepository.save(currentUser);
        return new MessageResponse("Privacy settings updated successfully");
    }

    @Transactional
    public MessageResponse updateProfilePicture(String profilePictureUrl) {
        User currentUser = getCurrentUser();
        currentUser.setProfilePicture(profilePictureUrl);
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        return new MessageResponse("Profile picture updated successfully");
    }

    @Transactional
    public MessageResponse updateCoverPhoto(String coverPhotoUrl) {
        User currentUser = getCurrentUser();
        currentUser.setCoverPhoto(coverPhotoUrl);
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        return new MessageResponse("Cover photo updated successfully");
    }

    @Transactional
    public MessageResponse deleteProfile() {
        User currentUser = getCurrentUser();

        // Instead of hard delete, you might want to soft delete or deactivate
        currentUser.setStatus("DELETED");
        currentUser.setEmail("deleted_" + currentUser.getId() + "@deleted.com");
        currentUser.setUsername("deleted_user_" + currentUser.getId());
        currentUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(currentUser);
        return new MessageResponse("Profile deactivated successfully");
    }

    // ========== FOLLOW/UNFOLLOW METHODS ==========

    @Transactional
    public MessageResponse followUser(String username) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (currentUser.getId().equals(targetUser.getId())) {
            return new MessageResponse("You cannot follow yourself");
        }

        // Check if already following
        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            return new MessageResponse("You are already following this user");
        }

        // Check if blocked
        if (isBlocked(currentUser, targetUser)) {
            return new MessageResponse("Cannot follow this user due to block restrictions");
        }

        // Check if target account is private and not following back
        if (targetUser.getIsPrivate() &&
                !followRepository.existsByFollowerAndFollowing(targetUser, currentUser)) {
            // Send follow request instead of direct follow (you can implement this later)
            return new MessageResponse("Follow request sent to private account");
        }

        Follow follow = new Follow(currentUser, targetUser);
        followRepository.save(follow);

        return new MessageResponse("You are now following " + targetUser.getUsername());
    }

    @Transactional
    public MessageResponse unfollowUser(String username) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .ifPresent(follow -> followRepository.delete(follow));

        return new MessageResponse("You have unfollowed " + targetUser.getUsername());
    }

    @Transactional
    public MessageResponse removeFollower(String username) {
        User currentUser = getCurrentUser();
        User followerUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        followRepository.findByFollowerAndFollowing(followerUser, currentUser)
                .ifPresent(follow -> followRepository.delete(follow));

        return new MessageResponse("Follower removed successfully");
    }

    @Transactional(readOnly = true)
    public List<User> getFollowers(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

        return followRepository.findFollowerUsers(user.getId(), pageable).getContent();
    }

    @Transactional(readOnly = true)
    public List<User> getFollowing(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

        return followRepository.findFollowingUsers(user.getId(), pageable).getContent();
    }

    @Transactional(readOnly = true)
    public Long getFollowersCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return followRepository.countByFollowing(user);
    }

    @Transactional(readOnly = true)
    public Long getFollowingCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return followRepository.countByFollower(user);
    }

    // ========== BLOCK/UNBLOCK METHODS ==========

    @Transactional
    public MessageResponse blockUser(String username) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (currentUser.getId().equals(targetUser.getId())) {
            return new MessageResponse("You cannot block yourself");
        }

        // Remove any follow relationship (both directions)
        followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .ifPresent(follow -> followRepository.delete(follow));
        followRepository.findByFollowerAndFollowing(targetUser, currentUser)
                .ifPresent(follow -> followRepository.delete(follow));

        // Check if already blocked
        if (blockRepository.existsByBlockerAndBlocked(currentUser, targetUser)) {
            return new MessageResponse("User is already blocked");
        }

        Block block = new Block(currentUser, targetUser);
        blockRepository.save(block);

        return new MessageResponse("User blocked successfully");
    }

    @Transactional
    public MessageResponse unblockUser(String username) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        blockRepository.findByBlockerAndBlocked(currentUser, targetUser)
                .ifPresent(block -> blockRepository.delete(block));

        return new MessageResponse("User unblocked successfully");
    }

    @Transactional(readOnly = true)
    public List<User> getBlockedUsers() {
        User currentUser = getCurrentUser();
        return blockRepository.findBlockedUsers(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public boolean isUserBlocked(String username) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return isBlocked(currentUser, targetUser);
    }

    // ========== PRIVACY CHECKS ==========

    @Transactional(readOnly = true)
    public boolean canViewProfile(String viewerUsername, String profileOwnerUsername) {
        User viewer = userRepository.findByUsername(viewerUsername)
                .orElseThrow(() -> new RuntimeException("Viewer not found: " + viewerUsername));
        User owner = userRepository.findByUsername(profileOwnerUsername)
                .orElseThrow(() -> new RuntimeException("Profile owner not found: " + profileOwnerUsername));

        // Can always view own profile
        if (viewer.getId().equals(owner.getId())) {
            return true;
        }

        // Check if blocked
        if (isBlocked(viewer, owner)) {
            return false;
        }

        // If profile is public, can view
        if (!owner.getIsPrivate()) {
            return true;
        }

        // If profile is private, check if viewer is a follower
        return followRepository.existsByFollowerAndFollowing(viewer, owner);
    }

    @Transactional(readOnly = true)
    public boolean canSendMessage(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + receiverUsername));

        // Check if blocked
        if (isBlocked(sender, receiver)) {
            return false;
        }

        // Check receiver's messaging preference
        if (receiver.getAllowMessagesFromAnyone()) {
            return true;
        }

        // If not allowing from anyone, must be a follower
        return followRepository.existsByFollowerAndFollowing(sender, receiver);
    }

    // ========== HELPER METHODS ==========

    private boolean isBlocked(User user1, User user2) {
        return blockRepository.existsByBlockerAndBlocked(user1, user2) ||
                blockRepository.existsByBlockerAndBlocked(user2, user1);
    }

    /**
     * Maps a User to EnhancedProfileResponse (public method for SearchService)
     */
    public EnhancedProfileResponse mapToEnhancedProfileResponse(User user, User currentUser) {
        EnhancedProfileResponse response = new EnhancedProfileResponse();

        // Basic info
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(filterByPrivacy(user.getEmail(), user.getShowEmail(), currentUser, user));
        response.setFullName(user.getFullName());
        response.setBio(user.getBio());
        response.setProfilePicture(user.getProfilePicture());
        response.setCoverPhoto(user.getCoverPhoto());
        response.setLocation(filterByPrivacy(user.getLocation(), user.getShowLocation(), currentUser, user));
        response.setWebsite(user.getWebsite());
        response.setUserType(user.getUserType());
        response.setStatus(user.getStatus());
        response.setIsPrivate(user.getIsPrivate());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastActive(filterLastActive(user, currentUser));
        response.setProfileViews(user.getProfileViews());

        // Enhanced fields
        response.setOccupation(user.getOccupation());
        response.setCompany(user.getCompany());
        response.setEducation(user.getEducation());
        response.setSkills(user.getSkills());
        response.setInterests(user.getInterests());
        response.setPhoneNumber(filterByPrivacy(user.getPhoneNumber(), user.getShowPhone(), currentUser, user));
        response.setTheme(user.getTheme());

        // Social links
        response.setFacebookUrl(user.getFacebookUrl());
        response.setTwitterUrl(user.getTwitterUrl());
        response.setInstagramUrl(user.getInstagramUrl());
        response.setLinkedinUrl(user.getLinkedinUrl());
        response.setGithubUrl(user.getGithubUrl());
        response.setYoutubeUrl(user.getYoutubeUrl());

        // Privacy settings (only visible to own profile)
        if (currentUser.getId().equals(user.getId())) {
            response.setShowEmail(user.getShowEmail());
            response.setShowPhone(user.getShowPhone());
            response.setShowLocation(user.getShowLocation());
            response.setShowLastActive(user.getShowLastActive());
            response.setAllowMessagesFromAnyone(user.getAllowMessagesFromAnyone());
            response.setAllowTagging(user.getAllowTagging());
        }

        // Stats
        response.setFollowersCount(followRepository.countByFollowing(user));
        response.setFollowingCount(followRepository.countByFollower(user));
        response.setPostsCount(0L); // Will be implemented by post module

        // Relationship flags
        response.setIsFollowedByCurrentUser(
                !currentUser.getId().equals(user.getId()) &&
                        followRepository.existsByFollowerAndFollowing(currentUser, user)
        );
        response.setIsFollowingCurrentUser(
                !currentUser.getId().equals(user.getId()) &&
                        followRepository.existsByFollowerAndFollowing(user, currentUser)
        );
        response.setIsBlocked(isBlocked(currentUser, user));

        return response;
    }

    private String filterByPrivacy(String value, Boolean show, User currentUser, User profileUser) {
        if (currentUser.getId().equals(profileUser.getId())) {
            return value; // Own profile - show everything
        }
        if (show != null && show) {
            return value; // User allowed to show this
        }
        return null; // Hide the information
    }

    private LocalDateTime filterLastActive(User profileUser, User currentUser) {
        if (currentUser.getId().equals(profileUser.getId())) {
            return profileUser.getLastActive(); // Own profile - show exact time
        }
        if (profileUser.getShowLastActive() != null && profileUser.getShowLastActive()) {
            return profileUser.getLastActive(); // User allowed to show last active
        }
        return null; // Hide last active
    }

    // ========== ADDITIONAL UTILITY METHODS ==========

    @Transactional
    public void updateLastActive() {
        User currentUser = getCurrentUser();
        currentUser.setLastActive(LocalDateTime.now());
        userRepository.save(currentUser);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + followerUsername));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + followingUsername));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    @Transactional(readOnly = true)
    public List<User> getSuggestedUsers(int limit) {
        User currentUser = getCurrentUser();
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(0, limit);
        return userRepository.findSuggestions(currentUser.getId(), pageable).getContent();
    }

    // ========== DEACTIVATION/REACTIVATION METHODS ==========

    @Transactional
    public MessageResponse deactivateAccount(String reason) {
        User currentUser = getCurrentUser();

        // Soft delete - mark as deleted but keep in database
        currentUser.setDeleted(true);
        currentUser.setDeactivationReason(reason);
        currentUser.setDeletedAt(LocalDateTime.now());
        currentUser.setStatus("DEACTIVATED");

        // Anonymize email and username to allow re-registration
        currentUser.setEmail("deleted_" + currentUser.getId() + "@deleted.com");
        currentUser.setUsername("user_" + currentUser.getId());

        userRepository.save(currentUser);

        // Clear security context to logout the user
        SecurityContextHolder.clearContext();

        return new MessageResponse("Account deactivated successfully. Sorry to see you go!");
    }

    @Transactional
    public MessageResponse reactivateAccount(String username, String password) {
        // Find user by original username (before anonymization)
        // Note: This assumes you have a way to find the user. You might need to use email or another identifier
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getDeleted()) {
            return new MessageResponse("Account is already active");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new MessageResponse("Invalid password");
        }

        // Reactivate account
        user.setDeleted(false);
        user.setDeactivationReason(null);
        user.setDeletedAt(null);
        user.setStatus("ACTIVE");
        // Note: You might want to restore original username/email here
        // This would require storing them somewhere or having a separate reactivation process
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return new MessageResponse("Account reactivated successfully! Welcome back!");
    }
}