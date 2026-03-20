package com.revconnect.controller;

import com.revconnect.dto.RegisterDTO;
import com.revconnect.service.UserService;
import com.revconnect.repository.SystemSettingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final SystemSettingRepository systemSettingRepository;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password");
        if (logout != null) model.addAttribute("message", "Logged out successfully");
        return "auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        boolean signupBlocked = systemSettingRepository.findBySettingKey("BLOCK_SIGNUP")
                .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                .orElse(false);

        model.addAttribute("registerDTO", new RegisterDTO());
        model.addAttribute("signupBlocked", signupBlocked);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // ── PASSWORD MATCH CHECK ──────────────────────────────────────────
        // Check BEFORE result.hasErrors() so it shows as a field-level error
        // right next to the Confirm Password input field
        if (dto.getPassword() != null && dto.getConfirmPassword() != null
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword",
                    "Passwords do not match");
        }
        // ─────────────────────────────────────────────────────────────────

        if (result.hasErrors()) {
            boolean signupBlocked = systemSettingRepository.findBySettingKey("BLOCK_SIGNUP")
                    .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                    .orElse(false);
            model.addAttribute("registerDTO", dto);
            model.addAttribute("signupBlocked", signupBlocked);
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            boolean signupBlocked = systemSettingRepository.findBySettingKey("BLOCK_SIGNUP")
                    .map(s -> Boolean.parseBoolean(s.getSettingValue()))
                    .orElse(false);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", dto);
            model.addAttribute("signupBlocked", signupBlocked);
            return "auth/register";
        }
    }
}