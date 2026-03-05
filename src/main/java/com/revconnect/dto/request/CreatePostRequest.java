// src/main/java/com/revconnect/dto/request/CreatePostRequest.java
package com.revconnect.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreatePostRequest {

    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 2000, message = "Post content cannot exceed 2000 characters")
    private String content;

    private String hashtags; // optional comma-separated, e.g. "java,spring"

    // Getters & Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
}