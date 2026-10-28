package com.portal.service;

import com.portal.dto.ApplicationStatusUpdateDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.ApplicationStatusHistoryRepository;
import com.portal.repository.SavedJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobService jobService;
    private final StudentService studentService;
    private final ResumeStorageService resumeStorageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationStatusHistoryRepository historyRepository,
                              SavedJobRepository savedJobRepository,
                              JobService jobService,
                              StudentService studentService,
                              ResumeStorageService resumeStorageService,
                              NotificationService notificationService,
                              AuditLogService auditLogService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobService = jobService;
        this.studentService = studentService;
        this.resumeStorageService = resumeStorageService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Application applyForJob(Long jobId, String coverLetter, User student) {
        Job job = jobService.getJobById(jobId);

        // 1. Check if job is active and not expired
        if (job.getStatus() != JobStatus.ACTIVE || job.isExpired()) {
            throw new InvalidOperationException("This job opportunity is closed or the deadline has passed.");
        }

        // 2. Prevent duplicate applications
        if (applicationRepository.existsByJobAndStudent(job, student)) {
            throw new InvalidOperationException("You have already submitted an application for this opportunity.");
        }

        // 3. Ensure student has a resume uploaded
        Resume latestResume = resumeStorageService.getLatestResumeForStudent(student)
                .orElseThrow(() -> new InvalidOperationException("Please upload your resume to your profile before applying for jobs."));

        // 4. Verify eligibility criteria
        StudentProfile profile = studentService.getProfileByUser(student);
        if (!jobService.isStudentEligible(profile, job)) {
            throw new InvalidOperationException("You do not meet the minimum eligibility criteria (CGPA or Branch) for this role.");
        }

        // Create application
        Application application = new Application(job, student, latestResume, coverLetter);
        Application saved = applicationRepository.save(application);

        // Create status history entry
        ApplicationStatusHistory history = new ApplicationStatusHistory(
                saved,
                null,
                ApplicationStatus.APPLIED,
                "Application submitted by candidate",
                student
        );
        historyRepository.save(history);

        // Audit log
        auditLogService.log(student, "APPLY_JOB", "Application", saved.getId(),
                "Candidate " + student.getEmail() + " applied for " + job.getTitle() + " at " + job.getCompany().getName(), "127.0.0.1");

        // Notify recruiter
        notificationService.sendNotification(
                job.getRecruiter(),
                student,
                "New Applicant: " + job.getTitle(),
                student.getFullName() + " applied for " + job.getTitle(),
                "/recruiter/jobs/" + job.getId() + "/applicants",
                NotificationType.APPLICATION_UPDATE
        );

        return saved;
    }

    @Transactional
    public void withdrawApplication(Long applicationId, User student) {
        Application application = getApplicationById(applicationId);

        // Ownership check
        if (!application.getStudent().getId().equals(student.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to withdraw this application.");
        }

        // Business rule: only allowed to withdraw if status is APPLIED or UNDER_REVIEW
        if (application.getStatus() != ApplicationStatus.APPLIED && application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new InvalidOperationException("Cannot withdraw application once shortlisted, interviewed, or finalized.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory(
                application,
                oldStatus,
                ApplicationStatus.WITHDRAWN,
                "Candidate withdrew application voluntarily",
                student
        );
        historyRepository.save(history);

        auditLogService.log(student, "WITHDRAW_APPLICATION", "Application", application.getId(),
                "Candidate withdrew application for " + application.getJob().getTitle(), "127.0.0.1");
    }

    @Transactional
    public Application updateStatus(Long applicationId, ApplicationStatusUpdateDto dto, User currentUser) {
        Application application = getApplicationById(applicationId);
        Job job = application.getJob();

        // Ownership check: must be job recruiter or admin
        if (!job.getRecruiter().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("You are not authorized to update applications for this job.");
        }

        ApplicationStatus oldStatus = application.getStatus();
        if (oldStatus == ApplicationStatus.WITHDRAWN) {
            throw new InvalidOperationException("Cannot change status of a withdrawn application.");
        }

        application.setStatus(dto.getStatus());
        Application saved = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory(
                saved,
                oldStatus,
                dto.getStatus(),
                dto.getRemarks() != null ? dto.getRemarks() : "Status updated by recruiter",
                currentUser
        );
        historyRepository.save(history);

        auditLogService.log(currentUser, "UPDATE_APPLICATION_STATUS", "Application", saved.getId(),
                "Status changed from " + oldStatus + " to " + dto.getStatus() + " for applicant " + application.getStudent().getFullName(), "127.0.0.1");

        // Notify student of status update
        String title = "Application Update: " + job.getTitle();
        String message = "Your application status for " + job.getTitle() + " at " + job.getCompany().getName() + " is now: " + dto.getStatus().getLabel();
        notificationService.sendNotification(
                application.getStudent(),
                currentUser,
                title,
                message,
                "/student/applications",
                NotificationType.APPLICATION_UPDATE
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application record not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Application> getStudentApplications(User student) {
        return applicationRepository.findByStudentOrderByAppliedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public List<Application> getJobApplications(Long jobId, User recruiter) {
        Job job = jobService.getJobById(jobId);
        if (!job.getRecruiter().getId().equals(recruiter.getId()) && recruiter.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("You are not authorized to view applicants for this job.");
        }
        return applicationRepository.findByJobOrderByAppliedAtDesc(job);
    }

    @Transactional(readOnly = true)
    public List<Application> getRecruiterApplications(User recruiter) {
        return applicationRepository.findByRecruiter(recruiter);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistory> getApplicationHistory(Application application) {
        return historyRepository.findByApplicationOrderByChangedAtDesc(application);
    }

    @Transactional(readOnly = true)
    public boolean hasStudentApplied(Long jobId, User student) {
        Job job = jobService.getJobById(jobId);
        return applicationRepository.existsByJobAndStudent(job, student);
    }

    // ----------------------------------------------------
    // SAVED JOBS WORKFLOW
    // ----------------------------------------------------
    @Transactional
    public void saveJob(Long jobId, User student) {
        Job job = jobService.getJobById(jobId);
        if (!savedJobRepository.existsByStudentAndJob(student, job)) {
            SavedJob savedJob = new SavedJob(student, job);
            savedJobRepository.save(savedJob);
        }
    }

    @Transactional
    public void unsaveJob(Long jobId, User student) {
        Job job = jobService.getJobById(jobId);
        savedJobRepository.deleteByStudentAndJob(student, job);
    }

    @Transactional(readOnly = true)
    public List<SavedJob> getSavedJobsForStudent(User student) {
        return savedJobRepository.findByStudentOrderBySavedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public boolean isJobSaved(Long jobId, User student) {
        Job job = jobService.getJobById(jobId);
        return savedJobRepository.existsByStudentAndJob(student, job);
    }
}
