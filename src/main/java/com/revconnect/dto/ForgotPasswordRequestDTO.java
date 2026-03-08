package com.revconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "Username or Email is required")
    private String usernameOrEmail;
}
