package com.revconnect.service;

import com.revconnect.dto.request.ForgotPasswordRequest;
import com.revconnect.dto.request.ResetPasswordRequest;
import com.revconnect.dto.response.MessageResponse;
import com.revconnect.model.user.PasswordResetToken;
import com.revconnect.model.user.User;
import com.revconnect.repository.PasswordResetTokenRepository;
import com.revconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.reset-password-token-expiration}")
    private long tokenExpiration;

    public PasswordService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Create new reset token
        PasswordResetToken resetToken = new PasswordResetToken(user);
        tokenRepository.save(resetToken);

        // Send email with reset link
        sendResetPasswordEmail(user.getEmail(), resetToken.getToken());

        return new MessageResponse("Password reset link has been sent to your email");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is expired
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        // Check if token was already used
        if (resetToken.isUsed()) {
            throw new RuntimeException("This reset token has already been used");
        }

        // Get user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return new MessageResponse("Password has been reset successfully");
    }

    public boolean validateResetToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isExpired() && !resetToken.isUsed())
                .orElse(false);
    }

    private void sendResetPasswordEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Your RevConnect Password");
        message.setText(
                "Hello,\n\n" +
                        "We received a request to reset your password for your RevConnect account.\n\n" +
                        "Click the link below to reset your password:\n" +
                        "http://localhost:8081/reset-password?token=" + token + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "Thanks,\n" +
                        "The RevConnect Team"
        );

        mailSender.send(message);
    }
}