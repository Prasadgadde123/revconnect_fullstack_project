package com.revconnect.controller;

import com.revconnect.dto.ReportRequestDTO;
import com.revconnect.entity.User;
import com.revconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDTO dto,
            @AuthenticationPrincipal User user) {
        if (user == null)
            return ResponseEntity.status(401).build();

        adminService.createReport(user, dto.getType(), dto.getTargetId(), dto.getReason());
        return ResponseEntity.ok().body("Report submitted successfully");
    }
}
