package com.revconnect;

import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.User;
import com.revconnect.enums.NotificationType;
import com.revconnect.enums.UserRole;
import com.revconnect.service.NotificationService;
import com.revconnect.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationServiceTest {

    @Autowired private NotificationService notificationService;
    @Autowired private UserService userService;

    private User recipient;
    private User actor;

    @BeforeEach
    void setUp() {
        recipient = userService.register(RegisterDTO.builder()
                .username("recip").email("r@test.com")
                .password("pass").confirmPassword("pass")
                .role(UserRole.PERSONAL).build());
        actor = userService.register(RegisterDTO.builder()
                .username("actor").email("a@test.com")
                .password("pass").confirmPassword("pass")
                .role(UserRole.PERSONAL).build());
    }

    @Test
    void testMarkAsRead() {
        notificationService.createNotification(recipient, actor, NotificationType.POST_LIKED, "Liked your post", "/post/1");
        var notifications = notificationService.getNotifications(recipient);
        assertFalse(notifications.isEmpty());
        Long id = notifications.get(0).getId();

        notificationService.markAsRead(id, recipient);
        var updated = notificationService.getNotifications(recipient).get(0);
        assertTrue(updated.isRead());
    }
}
