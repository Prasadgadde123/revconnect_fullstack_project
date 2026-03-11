package com.revconnect;

import com.revconnect.dto.ProfileUpdateDTO;
import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.UserRole;
import com.revconnect.repository.UserRepository;
import com.revconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private RegisterDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = RegisterDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.PERSONAL)
                .securityQuestion("What is your pet's name?")
                .securityAnswer("Buddy")
                .build();
    }

    @Test
    void testRegisterUser_Success() {
        User user = userService.register(validDto);
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername());
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }

    @Test
    void testRegisterUser_DuplicateUsername_Throws() {
        userService.register(validDto);
        RegisterDTO duplicate = RegisterDTO.builder()
                .username("testuser")
                .email("different@example.com")
                .password("pass")
                .confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .build();
        assertThrows(IllegalArgumentException.class, () -> userService.register(duplicate));
    }

    @Test
    void testRegisterUser_DuplicateEmail_Throws() {
        userService.register(validDto);
        RegisterDTO duplicate = RegisterDTO.builder()
                .username("newuser")
                .email("test@example.com")
                .password("pass")
                .confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .build();
        assertThrows(IllegalArgumentException.class, () -> userService.register(duplicate));
    }

    @Test
    void testLoadUserByUsername() {
        userService.register(validDto);
        var userDetails = userService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testFollowUnfollow() {
        User follower = userService.register(validDto);
        RegisterDTO dto2 = RegisterDTO.builder()
                .username("target").email("t@t.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.PERSONAL).build();
        User target = userService.register(dto2);

        assertFalse(userService.isFollowing(follower, target));
        userService.follow(follower, target);
        assertTrue(userService.isFollowing(follower, target));

        userService.unfollow(follower, target);
        assertFalse(userService.isFollowing(follower, target));
    }

    @Test
    void testUpdateProfile() {
        User user = userService.register(validDto);
        ProfileUpdateDTO updateDto = ProfileUpdateDTO.builder()
                .displayName("New Name")
                .bio("New Bio")
                .location("New Location")
                .build();
        User updated = userService.updateProfile(user, updateDto);
        assertEquals("New Name", updated.getDisplayName());
        assertEquals("New Bio", updated.getBio());
        assertEquals("New Location", updated.getLocation());
    }

    @Test
    void testSearchUsers() {
        userService.register(validDto);
        var results = userService.searchUsers("testuser");
        assertFalse(results.isEmpty());
        assertEquals("testuser", results.get(0).getUsername());
    }

    @Test
    void testGetSecurityQuestion_Success() {
        userService.register(validDto);
        String question = userService.getSecurityQuestion("testuser");
        assertEquals("What is your pet's name?", question);
    }

    @Test
    void testResetPassword_Success() {
        userService.register(validDto);
        com.revconnect.dto.ForgotPasswordResetDTO resetDto = com.revconnect.dto.ForgotPasswordResetDTO.builder()
                .usernameOrEmail("testuser")
                .securityAnswer("buddy ") // testing trim and case-insensitivity
                .newPassword("newpass123")
                .confirmPassword("newpass123")
                .build();

        userService.resetPasswordWithSecurityAnswer(resetDto);
        User user = userService.findByUsername("testuser");
        assertTrue(passwordEncoder.matches("newpass123", user.getPassword()));
    }

    @Test
    void testResetPassword_WrongAnswer_Throws() {
        userService.register(validDto);
        com.revconnect.dto.ForgotPasswordResetDTO resetDto = com.revconnect.dto.ForgotPasswordResetDTO.builder()
                .usernameOrEmail("testuser")
                .securityAnswer("Wrong")
                .newPassword("newpass123")
                .confirmPassword("newpass123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.resetPasswordWithSecurityAnswer(resetDto));
    }
}