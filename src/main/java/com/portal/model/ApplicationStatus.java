package com.portal.model;

public enum ApplicationStatus {
    APPLIED("Applied", "secondary"),
    UNDER_REVIEW("Under Review", "info"),
    SHORTLISTED("Shortlisted", "warning"),
    INTERVIEW_SCHEDULED("Interview Scheduled", "primary"),
    SELECTED("Selected / Offered", "success"),
    REJECTED("Rejected", "danger"),
    WITHDRAWN("Withdrawn", "dark");

    private final String label;
    private final String badgeColor;

    ApplicationStatus(String label, String badgeColor) {
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
