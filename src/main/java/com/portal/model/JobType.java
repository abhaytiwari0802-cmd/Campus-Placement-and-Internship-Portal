package com.portal.model;

public enum JobType {
    FULL_TIME("Full Time"),
    INTERNSHIP("Internship"),
    PART_TIME("Part Time"),
    CONTRACT("Contract");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return displayName;
    }
}
