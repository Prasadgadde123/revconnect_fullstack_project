package com.revconnect.dto.request;

public class PrivacySettingsRequest {
    private Boolean isPrivate;
    private Boolean showEmail;
    private Boolean showPhone;
    private Boolean showLocation;
    private Boolean showLastActive;
    private Boolean allowMessagesFromAnyone;
    private Boolean allowTagging;

    // Getters and Setters
    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    public Boolean getShowEmail() { return showEmail; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }

    public Boolean getShowPhone() { return showPhone; }
    public void setShowPhone(Boolean showPhone) { this.showPhone = showPhone; }

    public Boolean getShowLocation() { return showLocation; }
    public void setShowLocation(Boolean showLocation) { this.showLocation = showLocation; }

    public Boolean getShowLastActive() { return showLastActive; }
    public void setShowLastActive(Boolean showLastActive) { this.showLastActive = showLastActive; }

    public Boolean getAllowMessagesFromAnyone() { return allowMessagesFromAnyone; }
    public void setAllowMessagesFromAnyone(Boolean allowMessagesFromAnyone) { this.allowMessagesFromAnyone = allowMessagesFromAnyone; }

    public Boolean getAllowTagging() { return allowTagging; }
    public void setAllowTagging(Boolean allowTagging) { this.allowTagging = allowTagging; }
}