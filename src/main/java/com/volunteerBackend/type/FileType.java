package com.volunteerBackend.type;

public enum FileType {
    // USER_AVATAR("users/avatar"),
    // USER_COVER("users/cover"),
    // CAMPAIGN_IMAGE("campaigns"),
    // ORGANIZER_IMAGE("organizers"),
    // CAMPAIGN_DOCUMENT("campaign/documents");

    USER_AVATAR("userAvatar_uploads"),
    USER_COVER("userCover_uploads"),
    CAMPAIGN_IMAGE("campaign_uploads"),
    ORGANIZER_IMAGE("organizer_uploads"),
    CAMPAIGN_DOCUMENT("campaign/documents");
    private final String path;

    FileType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}