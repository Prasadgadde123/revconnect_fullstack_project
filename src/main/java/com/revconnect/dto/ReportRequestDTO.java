package com.revconnect.dto;

import com.revconnect.enums.ReportType;
import lombok.Data;

@Data
public class ReportRequestDTO {
    private ReportType type;
    private Long targetId;
    private String reason;
}
