package com.portal.service;

import com.portal.dto.InterviewScheduleDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.ApplicationStatusHistoryRepository;
import com.portal.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            ApplicationStatusHistoryRepository historyRepository,
                            NotificationService notificationService,
                            AuditLogService auditLogService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Interview scheduleInterview(InterviewScheduleDto dto, User recruiter) {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + dto.getApplicationId()));

        // Ownership check
        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId()) && recruiter.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("You are not authorized to schedule interviews for this application.");
        }

        Interview interview = new Interview(
                application,
                dto.getInterviewMode(),
                dto.getInterviewDate(),
                dto.getInterviewTime(),
                dto.getLocationOrLink().trim(),
                dto.getInterviewerName() != null ? dto.getInterviewerName().trim() : null,
                dto.getInstructions()
        );
        Interview saved = interviewRepository.save(interview);

        // Update application status to INTERVIEW_SCHEDULED
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        // Record status history
        ApplicationStatusHistory history = new ApplicationStatusHistory(
                application,
                oldStatus,
                ApplicationStatus.INTERVIEW_SCHEDULED,
                "Interview scheduled on " + dto.getInterviewDate() + " at " + dto.getInterviewTime() + " (" + dto.getInterviewMode().getLabel() + ")",
                recruiter
        );
        historyRepository.save(history);

        // Notify student
        String title = "Interview Scheduled: " + application.getJob().getTitle();
        String message = "Congratulations! An interview has been scheduled with " + application.getJob().getCompany().getName() +
                " on " + dto.getInterviewDate() + " at " + dto.getInterviewTime() + ". Details: " + dto.getLocationOrLink();
        notificationService.sendNotification(
                application.getStudent(),
                recruiter,
                title,
                message,
                "/student/interviews",
                NotificationType.INTERVIEW_SCHEDULED
        );

        auditLogService.log(recruiter, "SCHEDULE_INTERVIEW", "Interview", saved.getId(),
                "Scheduled interview for " + application.getStudent().getFullName() + " for job " + application.getJob().getTitle(), "127.0.0.1");

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Interview> getUpcomingInterviewsForStudent(User student) {
        return interviewRepository.findUpcomingByStudent(student, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Interview> getAllInterviewsForStudent(User student) {
        return interviewRepository.findByStudent(student);
    }

    @Transactional(readOnly = true)
    public List<Interview> getInterviewsByRecruiter(User recruiter) {
        return interviewRepository.findByRecruiter(recruiter);
    }

    @Transactional
    public void updateInterviewStatus(Long interviewId, InterviewStatus status, User recruiter) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview record not found with id: " + interviewId));

        if (!interview.getApplication().getJob().getRecruiter().getId().equals(recruiter.getId()) && recruiter.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedAccessException("Not authorized to update this interview status.");
        }

        interview.setStatus(status);
        interviewRepository.save(interview);

        auditLogService.log(recruiter, "UPDATE_INTERVIEW_STATUS", "Interview", interview.getId(),
                "Updated status to " + status + " for interview with " + interview.getApplication().getStudent().getFullName(), "127.0.0.1");
    }
}
