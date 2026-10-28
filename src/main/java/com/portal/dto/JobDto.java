package com.portal.dto;

import com.portal.model.JobStatus;
import com.portal.model.JobType;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class JobDto {

    private Long id;

    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Job description is required")
    private String description;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Salary/Stipend details are required")
    private String salaryOrStipend;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "10.0", inclusive = true)
    private BigDecimal minCgpa = BigDecimal.ZERO;

    private String allowedBranches;
    private String requiredSkills;

    @NotNull(message = "Number of openings is required")
    @Min(value = 1, message = "Openings must be at least 1")
    private Integer openings = 1;

    @NotNull(message = "Application deadline is required")
    @Future(message = "Deadline must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate deadline;

    private JobStatus status = JobStatus.ACTIVE;
    private Long companyId;
    private String companyName;

    public JobDto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSalaryOrStipend() {
        return salaryOrStipend;
    }

    public void setSalaryOrStipend(String salaryOrStipend) {
        this.salaryOrStipend = salaryOrStipend;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(BigDecimal minCgpa) {
        this.minCgpa = minCgpa;
    }

    public String getAllowedBranches() {
        return allowedBranches;
    }

    public void setAllowedBranches(String allowedBranches) {
        this.allowedBranches = allowedBranches;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public Integer getOpenings() {
        return openings;
    }

    public void setOpenings(Integer openings) {
        this.openings = openings;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
