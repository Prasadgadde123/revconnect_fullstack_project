package com.revconnect.service;

import com.revconnect.dto.ReportResponseDTO;

import com.revconnect.entity.Report;
import com.revconnect.entity.User;
import com.revconnect.entity.Post;
import com.revconnect.enums.ReportStatus;
import com.revconnect.enums.ReportType;
import com.revconnect.repository.ReportRepository;
import com.revconnect.repository.UserRepository;
import com.revconnect.repository.PostRepository;
import com.revconnect.repository.SystemSettingRepository;
import com.revconnect.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final NotificationService notificationService;

    public String getSetting(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(com.revconnect.entity.SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getSetting(key, String.valueOf(defaultValue)));
    }

    @Transactional
    public void updateSetting(String key, String value) {
        com.revconnect.entity.SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElse(com.revconnect.entity.SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        systemSettingRepository.save(setting);
        log.info("System setting updated: {} = {}", key, value);
    }

    public List<ReportResponseDTO> getAllReports() {
        log.debug("Fetching all reports");
        return reportRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ReportResponseDTO> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resolveReport(Long reportId, User resolver, String note) {
        log.info("Resolving report ID: {} by {}", reportId, resolver.getUsername());
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
        log.info("Dismissing report ID: {} by {}", reportId, resolver.getUsername());
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
        log.info("Creating report: Reporter={}, Type={}, Target={}, Reason={}",
                reporter.getUsername(), type, targetId, reason);

        Report report = Report.builder()
                .reporter(reporter)
                .type(type)
                .targetId(targetId)
                .reason(reason)
                .status(com.revconnect.enums.ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);
        log.info("Report saved to repository. ID={}", report.getId());

        // Notify all admins
        List<User> admins = userRepository.findByRole(com.revconnect.enums.UserRole.ADMIN);
        log.debug("Found {} admins to notify", admins.size());

        String message = String.format("🚨 NEW REPORT: %s reported a %s for: %s",
                reporter.getDisplayNameOrUsername(), type.name(), reason);

        /*
         * for (User admin : admins) {
         * notificationService.createNotification(admin, reporter,
         * com.revconnect.enums.NotificationType.REPORT,
         * message, "/admin/reports");
         * }
         */

    }

    public List<ReportResponseDTO> getRecentReports() {
        return reportRepository.findAll().stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(5)
                .map(this::convertToDTO)
                .toList();
    }

    private ReportResponseDTO convertToDTO(Report report) {
        ReportResponseDTO.ReportResponseDTOBuilder builder = ReportResponseDTO.builder()
                .id(report.getId())
                .reporterUsername(report.getReporter().getUsername())
                .reporterDisplayName(report.getReporter().getDisplayNameOrUsername())
                .type(report.getType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .resolutionNote(report.getResolutionNote());

        if (report.getResolver() != null) {
            builder.resolverUsername(report.getResolver().getUsername());
        }

        // Resolve Target Info
        if (report.getType() == ReportType.USER) {
            userRepository.findById(report.getTargetId()).ifPresent(user -> {
                builder.targetName(user.getDisplayNameOrUsername());
                builder.targetSnippet("@" + user.getUsername());
            });
        } else if (report.getType() == ReportType.POST) {
            postRepository.findById(report.getTargetId()).ifPresent(post -> {
                builder.targetName(
                        "Post by @" + (post.getAuthor() != null ? post.getAuthor().getUsername() : "deleted"));
                String content = post.getContent();
                builder.targetSnippet(content.length() > 50 ? content.substring(0, 47) + "..." : content);
            });
        }

        return builder.build();
    }
}
