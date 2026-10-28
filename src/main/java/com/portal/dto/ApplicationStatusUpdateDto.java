package com.portal.dto;

import com.portal.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class ApplicationStatusUpdateDto {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private String remarks;

    public ApplicationStatusUpdateDto() {
    }

    public ApplicationStatusUpdateDto(ApplicationStatus status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
