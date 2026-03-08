package com.revconnect.service;

import com.revconnect.dto.ForgotPasswordResetDTO;
import com.revconnect.dto.ProfileUpdateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import com.revconnect.exception.ResourceNotFoundException;
import com.revconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }



    public User register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .displayName(dto.getDisplayName())
                .role(dto.getRole() != null ? dto.getRole() : UserRole.PERSONAL)
                .securityQuestion(dto.getSecurityQuestion())
                .securityAnswer(passwordEncoder.encode(dto.getSecurityAnswer().toLowerCase().trim()))
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());
        return saved;
    }

    public String getSecurityQuestion(String usernameOrEmail) {
        User user = (User) loadUserByUsername(usernameOrEmail);
        if (user.getSecurityQuestion() == null) {
            throw new IllegalArgumentException("Security question not set for this account");
        }
        return user.getSecurityQuestion();
    }

    public void resetPasswordWithSecurityAnswer(ForgotPasswordResetDTO dto) {
        User user = (User) loadUserByUsername(dto.getUsernameOrEmail());

        if (!passwordEncoder.matches(dto.getSecurityAnswer().toLowerCase().trim(), user.getSecurityAnswer())) {
            throw new IllegalArgumentException("Incorrect security answer");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successful for user: {}", user.getUsername());
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public Optional<User> findByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateProfile(User user, ProfileUpdateDTO dto) {
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

        return userRepository.save(user);
    }

    public void updateProfilePicture(User user, String picturePath) {
        user.setProfilePicture(picturePath);
        userRepository.save(user);
    }

    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    public void follow(User follower, User target) {
        // Re-fetch within session to safely modify the following collection
        User freshFollower = userRepository.findByIdWithFollowing(follower.getId())
                .orElse(follower);
        if (!userRepository.isFollowing(follower.getId(), target.getId())) {
            freshFollower.getFollowing().add(target);
            userRepository.save(freshFollower);
            log.info("{} followed {}", follower.getUsername(), target.getUsername());
        }
    }

    public void unfollow(User follower, User target) {
        User freshFollower = userRepository.findByIdWithFollowing(follower.getId())
                .orElse(follower);
        freshFollower.getFollowing().remove(target);
        userRepository.save(freshFollower);
    }

    public boolean isFollowing(User follower, User target) {
        return userRepository.isFollowing(follower.getId(), target.getId());
    }

    public List<User> getFollowers(User user) {
        return userRepository.findFollowers(user.getId());
    }

    public List<User> getFollowing(User user) {
        return userRepository.findFollowing(user.getId());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void toggleUserEnabled(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }
}
