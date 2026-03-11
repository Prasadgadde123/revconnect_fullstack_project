package com.revconnect.dto;

import com.revconnect.enums.ReportStatus;
import com.revconnect.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private String reporterUsername;
    private String reporterDisplayName;
    private ReportType type;
    private Long targetId;
    private String targetName; // Username for USER, snippet for POST
    private String targetSnippet; // snippet of content
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolverUsername;
    private String resolutionNote;
}
