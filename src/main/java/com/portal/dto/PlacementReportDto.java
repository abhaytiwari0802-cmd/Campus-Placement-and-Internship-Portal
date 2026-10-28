package com.portal.dto;

public class PlacementReportDto {

    private String companyName;
    private String jobTitle;
    private String jobType;
    private long totalApplications;
    private long shortlistedCount;
    private long interviewsCount;
    private long selectedCount;

    public PlacementReportDto() {
    }

    public PlacementReportDto(String companyName, String jobTitle, String jobType, long totalApplications, long shortlistedCount, long interviewsCount, long selectedCount) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.jobType = jobType;
        this.totalApplications = totalApplications;
        this.shortlistedCount = shortlistedCount;
        this.interviewsCount = interviewsCount;
        this.selectedCount = selectedCount;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getShortlistedCount() {
        return shortlistedCount;
    }

    public void setShortlistedCount(long shortlistedCount) {
        this.shortlistedCount = shortlistedCount;
    }

    public long getInterviewsCount() {
        return interviewsCount;
    }

    public void setInterviewsCount(long interviewsCount) {
        this.interviewsCount = interviewsCount;
    }

    public long getSelectedCount() {
        return selectedCount;
    }

    public void setSelectedCount(long selectedCount) {
        this.selectedCount = selectedCount;
    }
}
