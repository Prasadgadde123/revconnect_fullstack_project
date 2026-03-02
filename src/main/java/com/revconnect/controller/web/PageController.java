package com.revconnect.controller.web;

import com.revconnect.dto.request.RegisterRequest;
import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Controller
public class PageController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("title", "Home");
        if (session.getAttribute("user") != null) {
            model.addAttribute("loggedIn", true);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/feed";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/feed";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute RegisterRequest registerRequest,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            System.out.println("========== PROCESSING REGISTRATION ==========");
            System.out.println("Username: " + registerRequest.getUsername());
            System.out.println("Email: " + registerRequest.getEmail());

            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                model.addAttribute("error", "Username is already taken!");
                model.addAttribute("registerRequest", registerRequest);
                return "auth/register";
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                model.addAttribute("error", "Email is already in use!");
                model.addAttribute("registerRequest", registerRequest);
                return "auth/register";
            }

            String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
            if (!registerRequest.getPassword().matches(passwordRegex)) {
                model.addAttribute("error", "Password must be at least 8 characters with uppercase, lowercase, number, and special character!");
                model.addAttribute("registerRequest", registerRequest);
                return "auth/register";
            }

            User user = new User(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    passwordEncoder.encode(registerRequest.getPassword())
            );

            user.setFullName(registerRequest.getFullName());
            user.setUserType(registerRequest.getUserType() != null ? registerRequest.getUserType() : "PERSONAL");

            // Set security questions
            user.setSecurityQuestion1(registerRequest.getSecurityQuestion1());
            user.setSecurityAnswer1(registerRequest.getSecurityAnswer1());
            user.setSecurityQuestion2(registerRequest.getSecurityQuestion2());
            user.setSecurityAnswer2(registerRequest.getSecurityAnswer2());
            user.setSecurityQuestion3(registerRequest.getSecurityQuestion3());
            user.setSecurityAnswer3(registerRequest.getSecurityAnswer3());

            userRepository.save(user);
            System.out.println("✅ User registered successfully: " + user.getUsername());

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";

        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("registerRequest", registerRequest);
            return "auth/register";
        }
    }

    // ========== FORGOT PASSWORD WITH SECURITY QUESTIONS ==========

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(HttpSession session) {
        System.out.println("========== SHOWING FORGOT PASSWORD PAGE ==========");
        if (session.getAttribute("user") != null) {
            return "redirect:/feed";
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/verify-email")
    public String verifyEmail(@RequestParam String email,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        System.out.println("========== VERIFY EMAIL CALLED ==========");
        System.out.println("Email received: " + email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            System.out.println("❌ User NOT found for email: " + email);
            redirectAttributes.addFlashAttribute("error", "Email not found!");
            return "redirect:/forgot-password";
        }

        System.out.println("✅ User found: " + user.getUsername());

        // Check if user has security questions
        if (user.getSecurityQuestion1() == null || user.getSecurityQuestion1().isEmpty()) {
            System.out.println("❌ No security questions set for this user!");
            redirectAttributes.addFlashAttribute("error", "No security questions set for this account! Please register again.");
            return "redirect:/forgot-password";
        }

        // Store in session
        session.setAttribute("resetEmail", email);
        session.setAttribute("securityQuestion1", user.getSecurityQuestion1());
        session.setAttribute("securityQuestion2", user.getSecurityQuestion2());
        session.setAttribute("securityQuestion3", user.getSecurityQuestion3());

        System.out.println("✅ Session attributes set. Redirecting to security questions...");
        return "redirect:/forgot-password/security-questions";
    }

    @GetMapping("/forgot-password/security-questions")
    public String showSecurityQuestionsPage(HttpSession session, Model model) {
        System.out.println("========== SHOWING SECURITY QUESTIONS PAGE ==========");

        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            System.out.println("❌ Email null, redirecting to forgot-password");
            return "redirect:/forgot-password";
        }

        String q1 = (String) session.getAttribute("securityQuestion1");
        String q2 = (String) session.getAttribute("securityQuestion2");
        String q3 = (String) session.getAttribute("securityQuestion3");

        if (q1 == null) {
            System.out.println("❌ Questions null, redirecting to forgot-password");
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        model.addAttribute("question1", q1);
        model.addAttribute("question2", q2);
        model.addAttribute("question3", q3);

        System.out.println("✅ Showing security questions page");
        return "auth/security-questions";
    }

    @PostMapping("/forgot-password/verify-answers")
    public String verifyAnswers(@RequestParam String email,
                                @RequestParam String answer1,
                                @RequestParam String answer2,
                                @RequestParam String answer3,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        System.out.println("========== VERIFYING ANSWERS ==========");
        String sessionEmail = (String) session.getAttribute("resetEmail");

        if (sessionEmail == null || !sessionEmail.equals(email)) {
            System.out.println("❌ Session email mismatch");
            return "redirect:/forgot-password";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/forgot-password";
        }

        boolean answersCorrect =
                user.getSecurityAnswer1() != null && user.getSecurityAnswer1().equalsIgnoreCase(answer1) &&
                        user.getSecurityAnswer2() != null && user.getSecurityAnswer2().equalsIgnoreCase(answer2) &&
                        user.getSecurityAnswer3() != null && user.getSecurityAnswer3().equalsIgnoreCase(answer3);

        if (!answersCorrect) {
            System.out.println("❌ Answers incorrect");
            redirectAttributes.addFlashAttribute("error", "Security answers are incorrect!");
            return "redirect:/forgot-password/security-questions";
        }

        session.setAttribute("answersVerified", true);
        System.out.println("✅ Answers verified, redirecting to reset password");
        return "redirect:/forgot-password/reset";
    }

    @GetMapping("/forgot-password/reset")
    public String showResetPasswordPage(HttpSession session, Model model) {
        Boolean verified = (Boolean) session.getAttribute("answersVerified");
        String email = (String) session.getAttribute("resetEmail");

        if (verified == null || !verified || email == null) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        return "auth/reset-password-questions";
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        System.out.println("========== RESETTING PASSWORD ==========");
        System.out.println("Email: " + email);

        Boolean verified = (Boolean) session.getAttribute("answersVerified");
        String sessionEmail = (String) session.getAttribute("resetEmail");

        if (verified == null || !verified || sessionEmail == null || !sessionEmail.equals(email)) {
            System.out.println("❌ Not verified");
            return "redirect:/forgot-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/forgot-password/reset";
        }

        // Validate password strength
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            redirectAttributes.addFlashAttribute("error", "Password must be 8+ chars with uppercase, lowercase, number, special!");
            return "redirect:/forgot-password/reset";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/forgot-password";
        }

        // Encode the new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        System.out.println("✅ Password reset successful for: " + user.getUsername());

        // Clear session
        session.removeAttribute("resetEmail");
        session.removeAttribute("securityQuestion1");
        session.removeAttribute("securityQuestion2");
        session.removeAttribute("securityQuestion3");
        session.removeAttribute("answersVerified");

        redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please login with your new password.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("========== LOGOUT CALLED ==========");
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully!");
        return "redirect:/login?logout=true";
    }

    @GetMapping("/feed")
    public String feed(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        model.addAttribute("posts", Collections.emptyList());
        model.addAttribute("title", "Feed");
        model.addAttribute("username", user.getUsername());
        return "feed/index";
    }

    @GetMapping("/profile/view")
    public String viewProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "profile/view";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "profile/edit";
    }

    @GetMapping("/check-session")
    @ResponseBody
    public String checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "No user in session. Session ID: " + session.getId();
        }
        return "User: " + user.getUsername() + " is logged in. Session ID: " + session.getId() +
                "\nEmail: " + user.getEmail() +
                "\nFull Name: " + user.getFullName();
    }

    @GetMapping("/test-security")
    @ResponseBody
    public String testSecurity() {
        return "Security is working! You can access this without login.";
    }

    @GetMapping("/debug-forgot")
    @ResponseBody
    public String debugForgot(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "User not found: " + email;
        }
        return "User: " + user.getUsername() +
                "\nQ1: " + user.getSecurityQuestion1() +
                "\nA1: " + user.getSecurityAnswer1() +
                "\nQ2: " + user.getSecurityQuestion2() +
                "\nA2: " + user.getSecurityAnswer2() +
                "\nQ3: " + user.getSecurityQuestion3() +
                "\nA3: " + user.getSecurityAnswer3();
    }
}