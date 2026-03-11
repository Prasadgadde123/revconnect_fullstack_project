package com.revconnect;

import com.revconnect.dto.RegisterDTO;
import com.revconnect.entity.Connection;
import com.revconnect.entity.User;
import com.revconnect.enums.ConnectionStatus;
import com.revconnect.enums.UserRole;
import com.revconnect.service.ConnectionService;
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
class ConnectionServiceTest {

    @Autowired private ConnectionService connectionService;
    @Autowired private UserService userService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userService.register(RegisterDTO.builder()
                .username("userA").email("a@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.PERSONAL).build());
        userB = userService.register(RegisterDTO.builder()
                .username("userB").email("b@test.com")
                .password("pass").confirmPassword("pass")
                .securityQuestion("Question").securityAnswer("Answer")
                .role(UserRole.PERSONAL).build());
    }

    @Test
    void testSendAndAcceptConnection() {
        Connection conn = connectionService.sendRequest(userA, userB);
        assertEquals(ConnectionStatus.PENDING, conn.getStatus());

        connectionService.acceptRequest(conn.getId(), userB);
        assertTrue(connectionService.areConnected(userA, userB));
    }

    @Test
    void testRejectConnection() {
        Connection conn = connectionService.sendRequest(userA, userB);
        connectionService.rejectRequest(conn.getId(), userB);
        assertFalse(connectionService.areConnected(userA, userB));

        Connection updated = connectionService.getStatus(userA, userB) != null ? conn : null;
        // Note: depends on implementation of getStatus
    }
}