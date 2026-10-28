package com.portal.service;

import com.portal.dto.JobDto;
import com.portal.dto.JobSearchCriteria;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.*;
import com.portal.repository.CompanyRepository;
import com.portal.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final RecruiterService recruiterService;
    private final AuditLogService auditLogService;

    public JobService(JobRepository jobRepository,
                      CompanyRepository companyRepository,
                      RecruiterService recruiterService,
                      AuditLogService auditLogService) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.recruiterService = recruiterService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Job> getActiveJobs() {
        return jobRepository.findActiveJobs(JobStatus.ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Job> searchJobs(JobSearchCriteria criteria) {
        String keyword = (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty())
                ? criteria.getKeyword().trim() : null;
        String location = (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty())
                ? criteria.getLocation().trim() : null;

        return jobRepository.searchJobs(keyword, criteria.getJobType(), location, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job opportunity not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByRecruiter(User recruiter) {
        return jobRepository.findByRecruiterOrderByCreatedAtDesc(recruiter);
    }

    @Transactional(readOnly = true)
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Transactional
    public Job createJob(JobDto dto, User recruiter) {
        RecruiterProfile profile = recruiterService.getProfileByUser(recruiter);
        Company company = profile.getCompany();

        if (company == null) {
            throw new InvalidOperationException("You must be associated with a registered company to post jobs.");
        }
        if (company.getApprovalStatus() != CompanyStatus.APPROVED) {
            throw new InvalidOperationException("Your company (" + company.getName() + ") is pending administrator approval. You can post opportunities once approved.");
        }

        Job job = new Job(
                company,
                recruiter,
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                dto.getJobType(),
                dto.getLocation().trim(),
                dto.getSalaryOrStipend().trim(),
                dto.getMinCgpa(),
                dto.getAllowedBranches(),
                dto.getRequiredSkills(),
                dto.getOpenings(),
                dto.getDeadline()
        );

        Job saved = jobRepository.save(job);
        auditLogService.log(recruiter, "CREATE_JOB", "Job", saved.getId(),
                "Posted job: " + saved.getTitle() + " for company: " + company.getName(), "127.0.0.1");

        return saved;
    }

    @Transactional
    public Job updateJob(Long jobId, JobDto dto, User recruiter) {
        Job job = getJobById(jobId);

        // Ownership check
        if (!job.getRecruiter().getId().equals(recruiter.getId()) && recruiter.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("You are not authorized to edit this job posting.");
        }

        job.setTitle(dto.getTitle().trim());
        job.setDescription(dto.getDescription().trim());
        job.setJobType(dto.getJobType());
        job.setLocation(dto.getLocation().trim());
        job.setSalaryOrStipend(dto.getSalaryOrStipend().trim());
        job.setMinCgpa(dto.getMinCgpa());
        job.setAllowedBranches(dto.getAllowedBranches());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setOpenings(dto.getOpenings());
        job.setDeadline(dto.getDeadline());

        if (dto.getStatus() != null) {
            job.setStatus(dto.getStatus());
        }

        Job updated = jobRepository.save(job);
        auditLogService.log(recruiter, "UPDATE_JOB", "Job", updated.getId(),
                "Updated job posting: " + updated.getTitle(), "127.0.0.1");

        return updated;
    }

    @Transactional
    public void toggleJobStatus(Long jobId, User recruiter) {
        Job job = getJobById(jobId);

        if (!job.getRecruiter().getId().equals(recruiter.getId()) && recruiter.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("You are not authorized to modify this job posting status.");
        }

        if (job.getStatus() == JobStatus.ACTIVE) {
            job.setStatus(JobStatus.CLOSED);
        } else {
            job.setStatus(JobStatus.ACTIVE);
        }

        jobRepository.save(job);
        auditLogService.log(recruiter, "TOGGLE_JOB_STATUS", "Job", job.getId(),
                "Changed status to " + job.getStatus() + " for job: " + job.getTitle(), "127.0.0.1");
    }

    public boolean isStudentEligible(StudentProfile studentProfile, Job job) {
        // Check CGPA
        if (job.getMinCgpa() != null && studentProfile.getCgpa().compareTo(job.getMinCgpa()) < 0) {
            return false;
        }

        // Check Branch if specified
        if (job.getAllowedBranches() != null && !job.getAllowedBranches().trim().isEmpty()) {
            List<String> branches = Arrays.stream(job.getAllowedBranches().split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            String studentBranch = studentProfile.getBranch() != null ? studentProfile.getBranch().trim().toLowerCase() : "";
            String studentCourse = studentProfile.getCourse() != null ? studentProfile.getCourse().trim().toLowerCase() : "";

            boolean match = branches.stream().anyMatch(b -> studentBranch.contains(b) || studentCourse.contains(b) || b.contains(studentBranch));
            if (!match) {
                return false;
            }
        }

        return true;
    }
}
