package com.volunteerBackend.type;

public enum FileType {
    USER_AVATAR("users/avatar"),
    USER_COVER("users/cover"),
    CAMPAIGN_IMAGE("campaigns"),
    ORGANIZER_IMAGE("organizers"),
    CAMPAIGN_DOCUMENT("campaign/documents");

    private final String path;

    FileType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}