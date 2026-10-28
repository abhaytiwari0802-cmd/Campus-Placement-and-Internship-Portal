package com.portal.model;

public enum InterviewMode {
    ONLINE("Online / Virtual"),
    IN_PERSON("In Person / Campus"),
    TELEPHONIC("Telephonic");

    private final String label;

    InterviewMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
