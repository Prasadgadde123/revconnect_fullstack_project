// src/main/java/com/revconnect/service/FileStorageService.java
package com.revconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    // Images saved to uploads/ folder next to your jar (outside src)
    private static final String UPLOAD_DIR = "uploads/profile-pictures/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + UPLOAD_DIR, e);
        }
    }

    /**
     * Saves the uploaded file to disk and returns the public URL path.
     */
    public String saveProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        // Get original extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR + filename);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }

        // Return the public URL path (served by Spring Boot)
        return "/uploads/profile-pictures/" + filename;
    }

    /**
     * Deletes old profile picture from disk if it was locally stored.
     */
    public void deleteOldPicture(String oldUrl) {
        if (oldUrl == null || !oldUrl.startsWith("/uploads/")) return;
        try {
            Path oldPath = Paths.get(oldUrl.substring(1)); // remove leading /
            Files.deleteIfExists(oldPath);
        } catch (IOException e) {
            // Log but don't fail if old file can't be deleted
            System.err.println("Could not delete old profile picture: " + e.getMessage());
        }
    }
}