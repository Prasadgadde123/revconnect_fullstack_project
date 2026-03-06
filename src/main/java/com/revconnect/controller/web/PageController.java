package com.revconnect.controller.web;

import com.revconnect.dto.request.RegisterRequest;
import com.revconnect.dto.request.UpdateEnhancedProfileRequest;
import com.revconnect.dto.request.UserSearchRequest;
import com.revconnect.dto.response.BusinessProfileResponse;   // ← your actual location
import com.revconnect.dto.response.EnhancedProfileResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.dto.response.PageResponse;
import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import com.revconnect.service.BusinessProfileService;
import com.revconnect.service.ProfileService;
import com.revconnect.service.PostService;
import com.revconnect.service.SearchService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final ProfileService        profileService;
    private final SearchService         searchService;
    private final BusinessProfileService businessProfileService;
    private final PostService           postService;

    public PageController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ProfileService profileService,
            SearchService searchService,
            BusinessProfileService businessProfileService,
            PostService postService) {
        this.userRepository         = userRepository;
        this.passwordEncoder        = passwordEncoder;
        this.profileService         = profileService;
        this.searchService          = searchService;
        this.businessProfileService = businessProfileService;
        this.postService            = postService;
    }

    // ═══════════════════════════════════════════
    // HOME
    // ═══════════════════════════════════════════

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("title", "Home");
        if (session.getAttribute("user") != null) {
            model.addAttribute("loggedIn", true);
        }
        return "index";
    }

    // ═══════════════════════════════════════════
    // AUTH
    // ═══════════════════════════════════════════

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/feed";
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) return "redirect:/feed";
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute RegisterRequest req,
                                      Model model,
                                      RedirectAttributes ra) {
        try {
            if (userRepository.existsByUsername(req.getUsername())) {
                model.addAttribute("error", "Username is already taken!");
                model.addAttribute("registerRequest", req);
                return "auth/register";
            }
            if (userRepository.existsByEmail(req.getEmail())) {
                model.addAttribute("error", "Email is already in use!");
                model.addAttribute("registerRequest", req);
                return "auth/register";
            }

            String pwRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
            if (!req.getPassword().matches(pwRegex)) {
                model.addAttribute("error",
                        "Password must be at least 8 characters with uppercase, lowercase, number, and special character!");
                model.addAttribute("registerRequest", req);
                return "auth/register";
            }

            User user = new User(req.getUsername(), req.getEmail(),
                    passwordEncoder.encode(req.getPassword()));
            user.setFullName(req.getFullName());
            user.setUserType(req.getUserType() != null ? req.getUserType() : "PERSONAL");
            user.setSecurityQuestion1(req.getSecurityQuestion1());
            user.setSecurityAnswer1(req.getSecurityAnswer1());
            user.setSecurityQuestion2(req.getSecurityQuestion2());
            user.setSecurityAnswer2(req.getSecurityAnswer2());
            user.setSecurityQuestion3(req.getSecurityQuestion3());
            user.setSecurityAnswer3(req.getSecurityAnswer3());
            userRepository.save(user);

            ra.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("registerRequest", req);
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("success", "You have been logged out successfully!");
        return "redirect:/login?logout=true";
    }

    // ═══════════════════════════════════════════
    // FORGOT PASSWORD
    // ═══════════════════════════════════════════

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/feed";
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/verify-email")
    public String verifyEmail(@RequestParam String email,
                              HttpSession session,
                              RedirectAttributes ra) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Email not found!");
            return "redirect:/forgot-password";
        }
        if (user.getSecurityQuestion1() == null || user.getSecurityQuestion1().isEmpty()) {
            ra.addFlashAttribute("error", "No security questions set for this account!");
            return "redirect:/forgot-password";
        }
        session.setAttribute("resetEmail",         email);
        session.setAttribute("securityQuestion1",  user.getSecurityQuestion1());
        session.setAttribute("securityQuestion2",  user.getSecurityQuestion2());
        session.setAttribute("securityQuestion3",  user.getSecurityQuestion3());
        return "redirect:/forgot-password/security-questions";
    }

    @GetMapping("/forgot-password/security-questions")
    public String showSecurityQuestionsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        String q1    = (String) session.getAttribute("securityQuestion1");
        if (email == null || q1 == null) return "redirect:/forgot-password";

        model.addAttribute("email",     email);
        model.addAttribute("question1", q1);
        model.addAttribute("question2", session.getAttribute("securityQuestion2"));
        model.addAttribute("question3", session.getAttribute("securityQuestion3"));
        return "auth/security-questions";
    }

    @PostMapping("/forgot-password/verify-answers")
    public String verifyAnswers(@RequestParam String email,
                                @RequestParam String answer1,
                                @RequestParam String answer2,
                                @RequestParam String answer3,
                                HttpSession session,
                                RedirectAttributes ra) {
        String sessionEmail = (String) session.getAttribute("resetEmail");
        if (sessionEmail == null || !sessionEmail.equals(email)) return "redirect:/forgot-password";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/forgot-password";

        boolean ok = user.getSecurityAnswer1() != null && user.getSecurityAnswer1().equalsIgnoreCase(answer1)
                && user.getSecurityAnswer2() != null && user.getSecurityAnswer2().equalsIgnoreCase(answer2)
                && user.getSecurityAnswer3() != null && user.getSecurityAnswer3().equalsIgnoreCase(answer3);

        if (!ok) {
            ra.addFlashAttribute("error", "Security answers are incorrect!");
            return "redirect:/forgot-password/security-questions";
        }
        session.setAttribute("answersVerified", true);
        return "redirect:/forgot-password/reset";
    }

    @GetMapping("/forgot-password/reset")
    public String showResetPasswordPage(HttpSession session, Model model) {
        Boolean verified = (Boolean) session.getAttribute("answersVerified");
        String  email    = (String)  session.getAttribute("resetEmail");
        if (verified == null || !verified || email == null) return "redirect:/forgot-password";
        model.addAttribute("email", email);
        return "auth/reset-password-questions";
    }

    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes ra) {
        Boolean verified     = (Boolean) session.getAttribute("answersVerified");
        String  sessionEmail = (String)  session.getAttribute("resetEmail");
        if (verified == null || !verified || sessionEmail == null || !sessionEmail.equals(email))
            return "redirect:/forgot-password";

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/forgot-password/reset";
        }
        String pwRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
        if (!newPassword.matches(pwRegex)) {
            ra.addFlashAttribute("error", "Password must be 8+ chars with uppercase, lowercase, number, special!");
            return "redirect:/forgot-password/reset";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/forgot-password";

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        session.removeAttribute("resetEmail");
        session.removeAttribute("securityQuestion1");
        session.removeAttribute("securityQuestion2");
        session.removeAttribute("securityQuestion3");
        session.removeAttribute("answersVerified");

        ra.addFlashAttribute("success", "Password reset successfully! Please login.");
        return "redirect:/login";
    }

    // ═══════════════════════════════════════════
    // PROFILE PAGES
    // NOTE: Specific /profile/* routes MUST come BEFORE /profile/{username}
    // ═══════════════════════════════════════════

    /**
     * Own profile view — linked from feed's "View Profile" button.
     * BUSINESS and CREATOR accounts are redirected to the enhanced business-profile-view.
     */
    @GetMapping("/profile/view")
    public String viewProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        User sessionUser = (User) session.getAttribute("user");
        String userType  = sessionUser.getUserType() != null ? sessionUser.getUserType() : "PERSONAL";

        try {
            EnhancedProfileResponse profile = profileService.getMyProfile();
            model.addAttribute("profile", profile);
            model.addAttribute("isOwnProfile", true);

            if ("BUSINESS".equalsIgnoreCase(userType) || "CREATOR".equalsIgnoreCase(userType)) {
                try {
                    BusinessProfileResponse bp = businessProfileService.getMyBusinessProfile();
                    model.addAttribute("businessProfile", bp);
                } catch (Exception ex) {
                    model.addAttribute("businessProfile", null);
                }
                model.addAttribute("postsCount", postService.countPostsByUser(sessionUser.getId()));
                try {
                    model.addAttribute("posts", postService.getPostsByUser(sessionUser.getId(), 0, 20).getContent());
                } catch (Exception ex) {
                    model.addAttribute("posts", java.util.Collections.emptyList());
                }
                return "profile/business-profile-view";
            }

            long pCount = postService.countPostsByUser(sessionUser.getId());
            model.addAttribute("postsCount", pCount);
            try {
                model.addAttribute("posts", postService.getPostsByUser(sessionUser.getId(), 0, 20).getContent());
            } catch (Exception ex) {
                model.addAttribute("posts", java.util.Collections.emptyList());
            }
            return "profile/view";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/feed?error=profileerror";
        }
    }

    /**
     * Basic edit — simple form (fallback, kept for compatibility).
     */
    @GetMapping("/profile/edit")
    public String editBasicProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        try {
            EnhancedProfileResponse profile = profileService.getMyProfile();
            model.addAttribute("profile", profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "profile/edit";
    }

    /**
     * Enhanced edit — routes to PERSONAL or BUSINESS/CREATOR page based on userType.
     */
    @GetMapping("/profile/enhanced/edit")
    public String editEnhancedProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        User sessionUser = (User) session.getAttribute("user");
        String userType  = sessionUser.getUserType() != null
                ? sessionUser.getUserType().toString()
                : "PERSONAL";

        try {
            EnhancedProfileResponse profile = profileService.getMyProfile();
            model.addAttribute("profile", profile);

            if ("BUSINESS".equalsIgnoreCase(userType) || "CREATOR".equalsIgnoreCase(userType)) {
                // Load business profile (may be null if not set up yet)
                try {
                    BusinessProfileResponse bp = businessProfileService.getMyBusinessProfile();
                    model.addAttribute("businessProfile", bp);
                } catch (Exception ex) {
                    model.addAttribute("businessProfile", null);
                }
                return "profile/business-profile-edit";   // → Business / Creator edit page
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "profile/personal-edit";   // → Personal edit page
    }

    /**
     * Business profile view page (own profile).
     */
    @GetMapping("/profile/business/view")
    public String viewBusinessProfile(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        try {
            User sessionUser = (User) session.getAttribute("user");
            EnhancedProfileResponse profile = profileService.getMyProfile();
            model.addAttribute("profile", profile);
            model.addAttribute("isOwnProfile", true);
            model.addAttribute("postsCount", postService.countPostsByUser(sessionUser.getId()));
            try {
                model.addAttribute("posts", postService.getPostsByUser(sessionUser.getId(), 0, 20).getContent());
            } catch (Exception ex) {
                model.addAttribute("posts", java.util.Collections.emptyList());
            }
            try {
                BusinessProfileResponse bp = businessProfileService.getMyBusinessProfile();
                model.addAttribute("businessProfile", bp);
            } catch (Exception ex) {
                model.addAttribute("businessProfile", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "profile/business-profile-view";
    }

    /**
     * Deactivate account page.
     */
    @GetMapping("/profile/deactivate")
    public String deactivateAccountPage(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "profile/deactivate-account";
    }

    /**
     * Privacy settings page.
     */
    @GetMapping("/profile/privacy")
    public String privacySettings(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        try {
            EnhancedProfileResponse profile = profileService.getMyProfile();
            model.addAttribute("profile", profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "profile/privacy-settings";
    }

    /**
     * Profile update POST (form-based, used by basic edit page).
     */
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute UpdateEnhancedProfileRequest request,
                                HttpSession session,
                                RedirectAttributes ra) {
        try {
            MessageResponse response = profileService.updateProfile(request);
            User currentUser = (User) session.getAttribute("user");
            User updatedUser = userRepository.findById(currentUser.getId()).orElse(null);
            if (updatedUser != null) session.setAttribute("user", updatedUser);
            ra.addFlashAttribute("success", response.getMessage());
            return "redirect:/profile/view";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Other user's profile — handles /profile/{username}.
     * Declared LAST so Spring picks the specific mappings above first.
     */
    @GetMapping("/profile/{username}")
    public String viewUserProfile(@PathVariable String username,
                                  Model model,
                                  HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        // Safety net for reserved path segments
        switch (username) {
            case "enhanced":
            case "edit":
            case "view":
            case "privacy":
            case "deactivate":
            case "settings":
            case "update":
            case "business":
                return "redirect:/feed";
        }

        User sessionUser = (User) session.getAttribute("user");

        // Own profile via username URL → redirect to clean /profile/view
        if (sessionUser.getUsername().equalsIgnoreCase(username)) {
            return "redirect:/profile/view";
        }

        try {
            EnhancedProfileResponse profile = profileService.getProfile(username);
            model.addAttribute("profile", profile);
            model.addAttribute("isOwnProfile", false);
            return "profile/enhanced-view";

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("blocked")) {
                return "redirect:/feed?error=blocked";
            }
            return "redirect:/feed?error=notfound";
        }
    }

    // ═══════════════════════════════════════════
    // DISCOVER
    // ═══════════════════════════════════════════

    @GetMapping("/discover")
    public String discover(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "discover";
    }

    // ═══════════════════════════════════════════
    // SEARCH
    // ═══════════════════════════════════════════

    @GetMapping("/search")
    public String searchUsers(@RequestParam String q,
                              @RequestParam(defaultValue = "0")  int page,
                              @RequestParam(defaultValue = "10") int size,
                              Model model,
                              HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        try {
            UserSearchRequest req = new UserSearchRequest();
            req.setQuery(q); req.setPage(page); req.setSize(size);
            PageResponse<EnhancedProfileResponse> results = searchService.searchVisibleUsers(req);
            model.addAttribute("query",         q);
            model.addAttribute("users",         results.getContent());
            model.addAttribute("currentPage",   results.getPage());
            model.addAttribute("totalPages",    results.getTotalPages());
            model.addAttribute("totalElements", results.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Search failed: " + e.getMessage());
        }
        return "search/results";
    }

    @GetMapping("/search/advanced")
    public String advancedSearch(@RequestParam String q,
                                 @RequestParam(required = false) String userType,
                                 @RequestParam(required = false) String location,
                                 @RequestParam(required = false) String occupation,
                                 @RequestParam(required = false) String company,
                                 @RequestParam(required = false) String education,
                                 @RequestParam(defaultValue = "0")  int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model,
                                 HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        try {
            PageResponse<EnhancedProfileResponse> results =
                    searchService.advancedSearch(q, userType, location, occupation, company,
                            education, page, size);
            model.addAttribute("query",         q);
            model.addAttribute("users",         results.getContent());
            model.addAttribute("currentPage",   results.getPage());
            model.addAttribute("totalPages",    results.getTotalPages());
            model.addAttribute("totalElements", results.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Advanced search failed: " + e.getMessage());
        }
        return "search/results";
    }

    // ═══════════════════════════════════════════
    // DEBUG / UTILITY  (remove before production)
    // ═══════════════════════════════════════════

    @GetMapping("/check-session")
    @ResponseBody
    public String checkSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "No user in session. ID: " + session.getId();
        return "User: " + user.getUsername()
                + " | Email: "    + user.getEmail()
                + " | FullName: " + user.getFullName()
                + " | Session: "  + session.getId();
    }

    @GetMapping("/test-security")
    @ResponseBody
    public String testSecurity() {
        return "Security is working!";
    }

    @GetMapping("/debug-forgot")
    @ResponseBody
    public String debugForgot(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "User not found: " + email;
        return "User: " + user.getUsername()
                + "\nQ1: " + user.getSecurityQuestion1() + " / A1: " + user.getSecurityAnswer1()
                + "\nQ2: " + user.getSecurityQuestion2() + " / A2: " + user.getSecurityAnswer2()
                + "\nQ3: " + user.getSecurityQuestion3() + " / A3: " + user.getSecurityAnswer3();
    }
}