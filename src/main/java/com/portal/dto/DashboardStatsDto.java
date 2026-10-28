package com.portal.dto;

import java.util.Map;

public class DashboardStatsDto {

    // Student stats
    private long totalApplications;
    private long savedJobsCount;
    private long upcomingInterviewsCount;
    private int profileCompletion;
    private Map<String, Long> applicationsByStatus;

    // Recruiter stats
    private long activeJobsCount;
    private long totalApplicantsCount;
    private long shortlistedCount;
    private long recruiterInterviewsCount;

    // Admin stats
    private long totalUsers;
    private long totalStudents;
    private long totalRecruiters;
    private long totalCompanies;
    private long pendingCompanyApprovals;
    private long totalActiveJobs;
    private long totalPlacedStudents;

    public DashboardStatsDto() {
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getSavedJobsCount() {
        return savedJobsCount;
    }

    public void setSavedJobsCount(long savedJobsCount) {
        this.savedJobsCount = savedJobsCount;
    }

    public long getUpcomingInterviewsCount() {
        return upcomingInterviewsCount;
    }

    public void setUpcomingInterviewsCount(long upcomingInterviewsCount) {
        this.upcomingInterviewsCount = upcomingInterviewsCount;
    }

    public int getProfileCompletion() {
        return profileCompletion;
    }

    public void setProfileCompletion(int profileCompletion) {
        this.profileCompletion = profileCompletion;
    }

    public Map<String, Long> getApplicationsByStatus() {
        return applicationsByStatus;
    }

    public void setApplicationsByStatus(Map<String, Long> applicationsByStatus) {
        this.applicationsByStatus = applicationsByStatus;
    }

    public long getActiveJobsCount() {
        return activeJobsCount;
    }

    public void setActiveJobsCount(long activeJobsCount) {
        this.activeJobsCount = activeJobsCount;
    }

    public long getTotalApplicantsCount() {
        return totalApplicantsCount;
    }

    public void setTotalApplicantsCount(long totalApplicantsCount) {
        this.totalApplicantsCount = totalApplicantsCount;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getRecruiterInterviewsCount() {
        return recruiterInterviewsCount;
    }

    public void setRecruiterInterviewsCount(long recruiterInterviewsCount) {
        this.recruiterInterviewsCount = recruiterInterviewsCount;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalRecruiters() {
        return totalRecruiters;
    }

    public void setTotalRecruiters(long totalRecruiters) {
        this.totalRecruiters = totalRecruiters;
    }

    public long getTotalCompanies() {
        return totalCompanies;
    }

    public void setTotalCompanies(long totalCompanies) {
        this.totalCompanies = totalCompanies;
    }

    public long getPendingCompanyApprovals() {
        return pendingCompanyApprovals;
    }

    public void setPendingCompanyApprovals(long pendingCompanyApprovals) {
        this.pendingCompanyApprovals = pendingCompanyApprovals;
    }

    public long getTotalActiveJobs() {
        return totalActiveJobs;
    }

    public void setTotalActiveJobs(long totalActiveJobs) {
        this.totalActiveJobs = totalActiveJobs;
    }

    public long getTotalPlacedStudents() {
        return totalPlacedStudents;
    }

    public void setTotalPlacedStudents(long totalPlacedStudents) {
        this.totalPlacedStudents = totalPlacedStudents;
    }
}
