package com.revconnect.service;

import com.revconnect.dto.ForgotPasswordResetDTO;
import com.revconnect.dto.ProfileUpdateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import com.revconnect.exception.ResourceNotFoundException;
import com.revconnect.repository.UserRepository;
import com.revconnect.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemSettingRepository systemSettingRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        logger.debug("Loading user by identifier: {}", identifier);
        return userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> {
                    logger.warn("User not found with identifier: {}", identifier);
                    return new UsernameNotFoundException("User not found with username or email: " + identifier);
                });
    }

    public User register(RegisterDTO dto) {
        logger.info("Registering new user: {}", dto.getUsername());

        boolean signupBlocked = systemSettingRepository.findBySettingKey("BLOCK_SIGNUP")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);

        if (signupBlocked) {
            logger.warn("Registration blocked by admin setting. Attempted by: {}", dto.getUsername());
            throw new IllegalArgumentException("New user registrations are currently disabled by the administrator.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            logger.warn("Registration failed - username already taken: {}", dto.getUsername());
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Registration failed - email already registered: {}", dto.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            logger.warn("Registration failed - passwords do not match for user: {}", dto.getUsername());
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .displayName(dto.getDisplayName())
                .bio(dto.getBio())
                .location(dto.getLocation())
                .website(dto.getWebsite())
                .privateProfile(dto.isPrivateProfile())
                .role(dto.getRole() != null ? dto.getRole() : UserRole.PERSONAL)
                .securityQuestion(dto.getSecurityQuestion())
                .securityAnswer(dto.getSecurityAnswer() != null ? passwordEncoder.encode(dto.getSecurityAnswer().toLowerCase().trim()) : null)
                .build();

        User saved = userRepository.save(user);
        logger.info("New user registered successfully: {} with role: {}", saved.getUsername(), saved.getRole());
        return saved;
    }

    public String getSecurityQuestion(String usernameOrEmail) {
        logger.debug("Fetching security question for: {}", usernameOrEmail);
        User user = (User) loadUserByUsername(usernameOrEmail);
        if (user.getSecurityQuestion() == null) {
            logger.warn("Security question not set for user: {}", usernameOrEmail);
            throw new IllegalArgumentException("Security question not set for this account");
        }
        return user.getSecurityQuestion();
    }

    public void resetPasswordWithSecurityAnswer(ForgotPasswordResetDTO dto) {
        logger.info("Password reset attempt for: {}", dto.getUsernameOrEmail());
        User user = (User) loadUserByUsername(dto.getUsernameOrEmail());

        if (!passwordEncoder.matches(dto.getSecurityAnswer().toLowerCase().trim(), user.getSecurityAnswer())) {
            logger.warn("Password reset failed - incorrect security answer for user: {}", dto.getUsernameOrEmail());
            throw new IllegalArgumentException("Incorrect security answer");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            logger.warn("Password reset failed - passwords do not match for user: {}", dto.getUsernameOrEmail());
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        logger.info("Password reset successful for user: {}", user.getUsername());
    }

    public User findById(Long id) {
        logger.debug("Finding user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found");
                });
    }

    public User findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username: {}", username);
                    return new ResourceNotFoundException("User not found: " + username);
                });
    }

    public Optional<User> findByUsernameOptional(String username) {
        logger.debug("Optional lookup for username: {}", username);
        return userRepository.findByUsername(username);
    }

    public User updateProfile(User user, ProfileUpdateDTO dto) {
        logger.info("Updating profile for user: {}", user.getUsername());

        if (dto.getDisplayName() != null) user.setDisplayName(dto.getDisplayName());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getLocation() != null) user.setLocation(dto.getLocation());
        if (dto.getWebsite() != null) user.setWebsite(dto.getWebsite());
        if (dto.getCategory() != null) user.setCategory(dto.getCategory());
        if (dto.getBusinessAddress() != null) user.setBusinessAddress(dto.getBusinessAddress());
        if (dto.getBusinessHours() != null) user.setBusinessHours(dto.getBusinessHours());
        if (dto.getContactEmail() != null) user.setContactEmail(dto.getContactEmail());
        if (dto.getContactPhone() != null) user.setContactPhone(dto.getContactPhone());
        user.setPrivateProfile(dto.isPrivateProfile());
        user.setNotifyConnectionRequests(dto.isNotifyConnectionRequests());
        user.setNotifyLikes(dto.isNotifyLikes());
        user.setNotifyComments(dto.isNotifyComments());
        user.setNotifyFollowers(dto.isNotifyFollowers());
        user.setNotifyShares(dto.isNotifyShares());

        if (dto.getExternalLinks() != null) {
            user.getExternalLinks().clear();
            for (String link : dto.getExternalLinks()) {
                if (link != null && !link.isBlank()) {
                    user.getExternalLinks().add(link.trim());
                }
            }
        }

        User updated = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", user.getUsername());
        return updated;
    }

    public void updateProfilePicture(User user, String picturePath) {
        logger.info("Updating profile picture for user: {}", user.getUsername());
        user.setProfilePicture(picturePath);
        userRepository.save(user);
        logger.debug("Profile picture updated to: {} for user: {}", picturePath, user.getUsername());
    }

    public List<User> searchUsers(String query) {
        logger.info("Searching users with query: {}", query);
        List<User> results = userRepository.searchUsers(query);
        logger.debug("Search for '{}' returned {} users", query, results.size());
        return results;
    }

    public List<User> adminSearchUsers(String query) {
        logger.info("Admin searching users with query: {}", query);
        if (query == null || query.isBlank())
            return getAllUsers();
        List<User> results = userRepository.adminSearchUsers(query.trim());
        logger.debug("Admin search for '{}' returned {} users", query, results.size());
        return results;
    }

    public void deleteUser(Long userId) {
        logger.warn("Admin deleting user with id: {}", userId);
        userRepository.deleteById(userId);
        logger.info("User with id: {} deleted by admin", userId);
    }

    public void adminUpdateUser(Long userId, String displayName, String email, boolean enabled) {
        logger.info("Admin updating user id: {}", userId);
        User user = findById(userId);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setEnabled(enabled);
        userRepository.save(user);
        logger.info("Admin updated user: {} - enabled: {}", user.getUsername(), enabled);
    }

    public void follow(User follower, User target) {
        logger.info("User: {} attempting to follow: {}", follower.getUsername(), target.getUsername());
        User freshFollower = userRepository.findByIdWithFollowing(follower.getId())
                .orElse(follower);
        if (!userRepository.isFollowing(follower.getId(), target.getId())) {
            freshFollower.getFollowing().add(target);
            userRepository.save(freshFollower);
            logger.info("User: {} is now following: {}", follower.getUsername(), target.getUsername());
        } else {
            logger.debug("User: {} is already following: {}", follower.getUsername(), target.getUsername());
        }
    }

    public void unfollow(User follower, User target) {
        logger.info("User: {} unfollowing: {}", follower.getUsername(), target.getUsername());
        User freshFollower = userRepository.findByIdWithFollowing(follower.getId())
                .orElse(follower);
        freshFollower.getFollowing().remove(target);
        userRepository.save(freshFollower);
        logger.info("User: {} unfollowed: {}", follower.getUsername(), target.getUsername());
    }

    public boolean isFollowing(User follower, User target) {
        boolean result = userRepository.isFollowing(follower.getId(), target.getId());
        logger.debug("isFollowing check: {} -> {} = {}", follower.getUsername(), target.getUsername(), result);
        return result;
    }

    public List<User> getFollowers(User user) {
        logger.debug("Fetching followers for user: {}", user.getUsername());
        List<User> followers = userRepository.findFollowers(user.getId());
        logger.debug("User: {} has {} followers", user.getUsername(), followers.size());
        return followers;
    }

    public List<User> getFollowing(User user) {
        logger.debug("Fetching following list for user: {}", user.getUsername());
        List<User> following = userRepository.findFollowing(user.getId());
        logger.debug("User: {} is following {} accounts", user.getUsername(), following.size());
        return following;
    }

    public List<User> getUsersByRole(UserRole role) {
        logger.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.info("Total users in system: {}", users.size());
        return users;
    }

    public void toggleUserEnabled(Long userId) {
        logger.info("Toggling enabled status for user id: {}", userId);
        User user = findById(userId);
        boolean newStatus = !user.isEnabled();
        user.setEnabled(newStatus);
        userRepository.save(user);
        logger.info("User: {} enabled status changed to: {}", user.getUsername(), newStatus);
    }

    public void toggleBookmark(User user, User target) {
        logger.info("User: {} toggling bookmark for: {}", user.getUsername(), target.getUsername());
        User freshUser = userRepository.findByIdWithBookmarks(user.getId())
                .orElse(user);
        if (userRepository.isBookmarked(user.getId(), target.getId())) {
            freshUser.getBookmarkedUsers().remove(target);
            logger.info("User: {} removed bookmark for: {}", user.getUsername(), target.getUsername());
        } else {
            freshUser.getBookmarkedUsers().add(target);
            logger.info("User: {} bookmarked: {}", user.getUsername(), target.getUsername());
        }
        userRepository.save(freshUser);
    }

    public boolean isBookmarked(User user, User target) {
        boolean result = userRepository.isBookmarked(user.getId(), target.getId());
        logger.debug("isBookmarked check: {} -> {} = {}", user.getUsername(), target.getUsername(), result);
        return result;
    }
}