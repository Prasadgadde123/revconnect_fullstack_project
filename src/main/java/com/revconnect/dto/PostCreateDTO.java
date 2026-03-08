package com.revconnect.dto;

import com.revconnect.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostCreateDTO {
    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 2000)
    private String content;
    private String hashtags;
    private PostType postType;
    private String ctaButtonText;
    private String ctaButtonUrl;
    private String taggedProducts;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
    private java.time.LocalDateTime scheduledAt;
}
