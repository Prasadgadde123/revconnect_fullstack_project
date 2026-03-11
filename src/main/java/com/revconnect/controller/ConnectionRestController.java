package com.revconnect.controller;

import com.revconnect.entity.User;
import com.revconnect.service.ConnectionService;
import com.revconnect.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionRestController {

    private final ConnectionService connectionService;
    private final UserService userService;

    @PostMapping("/request/{userId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {
        try {
            User target = userService.findById(userId);
            connectionService.sendRequest(currentUser, target);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Connection request sent",
                    "status", "PENDING"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }
}
