package com.portal.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String enrollmentNumber;

    @Column(nullable = false, length = 100)
    private String course;

    @Column(nullable = false, length = 100)
    private String branch;

    @Column(nullable = false)
    private Integer graduationYear;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(length = 255)
    private String linkedinUrl;

    @Column(nullable = false)
    private Integer profileCompletion = 60;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public StudentProfile() {
    }

    public StudentProfile(User user, String enrollmentNumber, String course, String branch, Integer graduationYear, BigDecimal cgpa) {
        this.user = user;
        this.enrollmentNumber = enrollmentNumber;
        this.course = course;
        this.branch = branch;
        this.graduationYear = graduationYear;
        this.cgpa = cgpa;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateProfileCompletion();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateProfileCompletion();
    }

    public void calculateProfileCompletion() {
        int score = 40; // Base: user + basic academic info
        if (skills != null && !skills.trim().isEmpty()) score += 15;
        if (projects != null && !projects.trim().isEmpty()) score += 15;
        if (certifications != null && !certifications.trim().isEmpty()) score += 15;
        if (linkedinUrl != null && !linkedinUrl.trim().isEmpty()) score += 15;
        this.profileCompletion = Math.min(score, 100);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Integer graduationYear) {
        this.graduationYear = graduationYear;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public void setCgpa(BigDecimal cgpa) {
        this.cgpa = cgpa;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public Integer getProfileCompletion() {
        return profileCompletion;
    }

    public void setProfileCompletion(Integer profileCompletion) {
        this.profileCompletion = profileCompletion;
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
}
