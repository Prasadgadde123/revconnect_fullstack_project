package com.revconnect.controller;

import com.revconnect.dto.ReportRequestDTO;
import com.revconnect.entity.User;
import com.revconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDTO dto,
            @AuthenticationPrincipal User user) {
        log.info("Received report request: Type={}, TargetID={}, User={}",
                dto.getType(), dto.getTargetId(), (user != null ? user.getUsername() : "ANONYMOUS"));

        if (user == null) {
            log.warn("Unauthorized attempt to submit report");
            return ResponseEntity.status(401).build();
        }

        try {
            adminService.createReport(user, dto.getType(), dto.getTargetId(), dto.getReason());
            log.info("Report created successfully");
            return ResponseEntity.ok().body("Report submitted successfully");
        } catch (Exception e) {
            log.error("Error creating report", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
