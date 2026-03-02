package com.revconnect.controller.auth;

import com.revconnect.dto.request.LoginRequest;
import com.revconnect.dto.request.RegisterRequest;
import com.revconnect.dto.response.JwtResponse;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.User;
import com.revconnect.repository.UserRepository;
import com.revconnect.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            System.out.println("========== LOGIN ATTEMPT ==========");
            System.out.println("Username/Email: " + loginRequest.getUsernameOrEmail());
            System.out.println("Password: [PROTECTED]");

            // First, check if user exists in database
            User user = userRepository.findByUsername(loginRequest.getUsernameOrEmail())
                    .orElse(userRepository.findByEmail(loginRequest.getUsernameOrEmail()).orElse(null));

            if (user == null) {
                System.out.println("❌ User not found in database");
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Invalid username or password"));
            }

            System.out.println("✅ User found in database: " + user.getUsername());
            System.out.println("Stored password hash: " + user.getPassword());

            // Verify password manually
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            System.out.println("Password matches: " + passwordMatches);

            if (!passwordMatches) {
                System.out.println("❌ Password does not match");
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Invalid username or password"));
            }

            // Proceed with authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    ));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Store user in session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userFullName", user.getFullName());

            System.out.println("✅ Login successful for: " + user.getUsername());
            System.out.println("Session ID: " + session.getId());

            return ResponseEntity.ok(new JwtResponse(
                    "dummy-jwt-token",
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    user.getUserType() != null ? user.getUserType() : "USER"));
        } catch (Exception e) {
            System.out.println("❌ Login failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("========== REGISTRATION ==========");
            System.out.println("Username: " + registerRequest.getUsername());
            System.out.println("Email: " + registerRequest.getEmail());

            // Check if username exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
            }

            // Check if email exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
            }

            // Validate password strength
            String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
            if (!registerRequest.getPassword().matches(passwordRegex)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Password must be at least 8 characters with uppercase, lowercase, number, and special character!"));
            }

            // Create new user account
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
            System.out.println("Encoded password: " + user.getPassword());

            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(!exists);
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(!exists);
    }

    @PostMapping("/debug-login")
    @ResponseBody
    public String debugLogin(@RequestParam String username, @RequestParam String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElse(userRepository.findByEmail(username).orElse(null));

            if (user == null) {
                return "❌ User not found: " + username;
            }

            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());

            return "✅ User found: " + user.getUsername() +
                    "\n📧 Email: " + user.getEmail() +
                    "\n🔑 Password in DB: " + user.getPassword() +
                    "\n✅ Password matches: " + passwordMatches +
                    "\n📝 Password entered: [PROTECTED]";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}