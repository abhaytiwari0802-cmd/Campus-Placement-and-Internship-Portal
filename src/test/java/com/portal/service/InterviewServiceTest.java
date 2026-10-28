package com.portal.service;

import com.portal.dto.InterviewScheduleDto;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.ApplicationStatusHistoryRepository;
import com.portal.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(
                interviewRepository,
                applicationRepository,
                historyRepository,
                notificationService,
                auditLogService
        );
    }

    @Test
    @DisplayName("Should successfully schedule interview, update application status, and notify student")
    void testScheduleInterview_Success() {
        User recruiter = new User("recruiter@msft.com", "pass", "Recruiter", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        recruiter.setId(15L);

        User student = new User("student@campus.edu", "pass", "Student", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        Company company = new Company("Microsoft", "IT", "https://msft.com", "Desc", "Hyderabad", CompanyStatus.APPROVED, recruiter);

        Job job = new Job(company, recruiter, "Cloud Associate", "Desc", JobType.FULL_TIME, "Hyderabad", "14 LPA",
                new BigDecimal("7.5"), "MCA", "Cloud", 3, LocalDate.now().plusDays(20));

        Application application = new Application(job, student, null, "Cover");
        application.setId(101L);
        application.setStatus(ApplicationStatus.SHORTLISTED);

        when(applicationRepository.findById(101L)).thenReturn(Optional.of(application));
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InterviewScheduleDto dto = new InterviewScheduleDto();
        dto.setApplicationId(101L);
        dto.setInterviewMode(InterviewMode.ONLINE);
        dto.setInterviewDate(LocalDate.now().plusDays(3));
        dto.setInterviewTime(LocalTime.of(14, 30));
        dto.setLocationOrLink("https://teams.microsoft.com/meet/123");
        dto.setInterviewerName("Lead Azure Architect");

        Interview scheduled = interviewService.scheduleInterview(dto, recruiter);

        assertNotNull(scheduled);
        assertEquals(InterviewStatus.SCHEDULED, scheduled.getStatus());
        assertEquals(ApplicationStatus.INTERVIEW_SCHEDULED, application.getStatus());
        verify(interviewRepository).save(any(Interview.class));
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any(ApplicationStatusHistory.class));
        verify(notificationService).sendNotification(eq(student), eq(recruiter), anyString(), anyString(), anyString(), eq(NotificationType.INTERVIEW_SCHEDULED));
    }

    @Test
    @DisplayName("Should prevent unauthorized recruiter from scheduling interview for another company's job")
    void testScheduleInterview_UnauthorizedRecruiter() {
        User ownerRecruiter = new User("owner@msft.com", "pass", "Owner", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        ownerRecruiter.setId(15L);

        User otherRecruiter = new User("other@other.com", "pass", "Other", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        otherRecruiter.setId(99L);

        Job job = new Job(null, ownerRecruiter, "Role", "Desc", JobType.FULL_TIME, "Loc", "Salary",
                BigDecimal.ZERO, null, null, 1, LocalDate.now().plusDays(10));

        Application application = new Application(job, null, null, "Cover");
        application.setId(200L);

        when(applicationRepository.findById(200L)).thenReturn(Optional.of(application));

        InterviewScheduleDto dto = new InterviewScheduleDto();
        dto.setApplicationId(200L);

        assertThrows(UnauthorizedAccessException.class, () -> interviewService.scheduleInterview(dto, otherRecruiter));
        verify(interviewRepository, never()).save(any());
    }
}
