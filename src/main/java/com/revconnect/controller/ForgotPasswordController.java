package com.revconnect.controller;

import com.revconnect.dto.ForgotPasswordRequestDTO;
import com.revconnect.dto.ForgotPasswordResetDTO;
import com.revconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> getSecurityQuestion(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        try {
            String question = userService.getSecurityQuestion(dto.getUsernameOrEmail());
            return ResponseEntity.ok(Map.of("securityQuestion", question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ForgotPasswordResetDTO dto) {
        try {
            userService.resetPasswordWithSecurityAnswer(dto);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
