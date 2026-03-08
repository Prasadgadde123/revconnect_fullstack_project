package com.revconnect.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileUpdateDTO {
    @Size(max = 60)
    private String displayName;
    @Size(max = 300)
    private String bio;
    private String location;
    private String website;
    private String category;
    private String businessAddress;
    private String businessHours;
    private String contactEmail;
    private String contactPhone;
    private boolean privateProfile;
    private boolean notifyConnectionRequests;
    private boolean notifyLikes;
    private boolean notifyComments;
    private boolean notifyFollowers;
    private boolean notifyShares;
    
    // Multiple links
    private java.util.List<String> externalLinks;
}
