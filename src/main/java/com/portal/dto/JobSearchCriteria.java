package com.portal.dto;

import com.portal.model.JobType;

public class JobSearchCriteria {

    private String keyword;
    private JobType jobType;
    private String location;

    public JobSearchCriteria() {
    }

    public JobSearchCriteria(String keyword, JobType jobType, String location) {
        this.keyword = keyword;
        this.jobType = jobType;
        this.location = location;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
}
