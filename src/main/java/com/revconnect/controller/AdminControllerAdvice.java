package com.revconnect.controller;

import com.revconnect.dto.ReportResponseDTO;
import com.revconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;

@ControllerAdvice(assignableTypes = AdminController.class)
@RequiredArgsConstructor
public class AdminControllerAdvice {

    private final AdminService adminService;

    @ModelAttribute
    public void addAdminAttributes(Model model) {
        List<ReportResponseDTO> pending = adminService.getPendingReports();
        model.addAttribute("sidebarPendingReportsCount", pending.size());
    }
}
