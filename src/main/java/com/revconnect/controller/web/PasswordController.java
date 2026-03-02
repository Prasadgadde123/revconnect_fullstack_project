package com.revconnect.controller.web;

import com.revconnect.service.PasswordService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        boolean isValid = passwordService.validateResetToken(token);
        if (!isValid) {
            return "redirect:/forgot-password?error=invalid";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }
}