package com.portal.model;

public enum InterviewStatus {
    SCHEDULED("Scheduled", "primary"),
    COMPLETED("Completed", "success"),
    CANCELLED("Cancelled", "danger"),
    RESCHEDULED("Rescheduled", "warning");

    private final String label;
    private final String badgeColor;

    InterviewStatus(String label, String badgeColor) {
        this.label = label;
        this.badgeColor = badgeColor;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeColor() {
        return badgeColor;
    }
}
