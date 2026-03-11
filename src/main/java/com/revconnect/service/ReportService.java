package com.revconnect.service;

import com.revconnect.entity.Report;
import com.revconnect.entity.User;
import com.revconnect.entity.Post;
import com.revconnect.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report createReport(User reporter, User reportedUser, Post reportedPost, String reason) {
        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .reportedPost(reportedPost)
                .reason(reason)
                .status(Report.ReportStatus.PENDING)
                .build();
        return reportRepository.save(report);
    }

    public void resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(Report.ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    public void dismissReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(Report.ReportStatus.DISMISSED);
        reportRepository.save(report);
    }
}
