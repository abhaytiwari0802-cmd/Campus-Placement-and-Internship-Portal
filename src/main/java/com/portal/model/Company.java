package com.portal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String location;

    @Column(length = 255)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CompanyStatus approvalStatus = CompanyStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_recruiter_id")
    private User createdByRecruiter;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Company() {
    }

    public Company(String name, String industry, String website, String description, String location, CompanyStatus approvalStatus, User createdByRecruiter) {
        this.name = name;
        this.industry = industry;
        this.website = website;
        this.description = description;
        this.location = location;
        this.approvalStatus = approvalStatus;
        this.createdByRecruiter = createdByRecruiter;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (approvalStatus == null) {
            approvalStatus = CompanyStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public CompanyStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(CompanyStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public User getCreatedByRecruiter() {
        return createdByRecruiter;
    }

    public void setCreatedByRecruiter(User createdByRecruiter) {
        this.createdByRecruiter = createdByRecruiter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Transient
    public java.util.List<?> getJobs() {
        return java.util.Collections.emptyList();
    }
}
