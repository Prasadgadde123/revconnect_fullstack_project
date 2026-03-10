package com.revconnect.service;

import com.revconnect.entity.Report;
import com.revconnect.entity.User;
import com.revconnect.enums.ReportStatus;
import com.revconnect.repository.ReportRepository;
import com.revconnect.repository.UserRepository;
import com.revconnect.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING);
    }

    @Transactional
    public void resolveReport(Long reportId, User resolver, String note) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(ReportStatus.RESOLVED);
        report.setResolver(resolver);
        report.setResolutionNote(note);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Transactional
    public void dismissReport(Long reportId, User resolver) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(ReportStatus.DISMISSED);
        report.setResolver(resolver);
        report.setResolvedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPosts", postRepository.count());
        stats.put("pendingReports", (long) reportRepository.findByStatus(ReportStatus.PENDING).size());

        // Detailed breakdowns
        stats.put("personalUsers", userRepository.countByRole(com.revconnect.enums.UserRole.PERSONAL));
        stats.put("creatorUsers", userRepository.countByRole(com.revconnect.enums.UserRole.CREATOR));
        stats.put("businessUsers", userRepository.countByRole(com.revconnect.enums.UserRole.BUSINESS));

        return stats;
    }

    @Transactional
    public void broadcastAnnouncement(String message) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            notificationService.createNotification(user, null, com.revconnect.enums.NotificationType.SYSTEM,
                    "📢 ADMIN ANNOUNCEMENT: " + message, "/feed");
        }
    }

    @Transactional
    public void createReport(User reporter, com.revconnect.enums.ReportType type, Long targetId, String reason) {
        Report report = Report.builder()
                .reporter(reporter)
                .type(type)
                .targetId(targetId)
                .reason(reason)
                .status(com.revconnect.enums.ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        // Notify all admins
        List<User> admins = userRepository.findByRole(com.revconnect.enums.UserRole.ADMIN);
        String message = String.format("🚨 NEW REPORT: %s reported a %s for: %s",
                reporter.getDisplayNameOrUsername(), type.name(), reason);

        for (User admin : admins) {
            notificationService.createNotification(admin, reporter, com.revconnect.enums.NotificationType.ADMIN_REPORT,
                    message, "/admin/reports");
        }
    }
}
