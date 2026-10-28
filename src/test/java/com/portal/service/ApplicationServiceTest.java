package com.portal.service;

import com.portal.dto.ApplicationStatusUpdateDto;
import com.portal.exception.InvalidOperationException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.ApplicationStatusHistoryRepository;
import com.portal.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusHistoryRepository historyRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private JobService jobService;

    @Mock
    private StudentService studentService;

    @Mock
    private ResumeStorageService resumeStorageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                applicationRepository,
                historyRepository,
                savedJobRepository,
                jobService,
                studentService,
                resumeStorageService,
                notificationService,
                auditLogService
        );
    }

    @Test
    @DisplayName("Should successfully submit application when eligible and resume exists")
    void testApplyForJob_Success() {
        User student = new User("student@campus.edu", "pass", "Rahul", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        StudentProfile profile = new StudentProfile(student, "MCA/001", "MCA", "CS", 2026, new BigDecimal("8.50"));

        Company company = new Company("Google", "IT", "https://google.com", "Desc", "Bangalore", CompanyStatus.APPROVED, null);
        User recruiter = new User("recruiter@google.com", "pass", "Sundar", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        Job job = new Job(company, recruiter, "SDE-1", "Desc", JobType.FULL_TIME, "Bangalore", "20 LPA",
                new BigDecimal("8.00"), "MCA", "Java", 5, LocalDate.now().plusDays(20));

        Resume resume = new Resume(student, "resume.pdf", "resumes/stored.pdf", "application/pdf", 1024L);

        when(jobService.getJobById(1L)).thenReturn(job);
        when(applicationRepository.existsByJobAndStudent(job, student)).thenReturn(false);
        when(resumeStorageService.getLatestResumeForStudent(student)).thenReturn(Optional.of(resume));
        when(studentService.getProfileByUser(student)).thenReturn(profile);
        when(jobService.isStudentEligible(profile, job)).thenReturn(true);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Application application = applicationService.applyForJob(1L, "Interested in Google SDE", student);

        assertNotNull(application);
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(student, application.getStudent());
        assertEquals(job, application.getJob());
        verify(applicationRepository).save(any(Application.class));
        verify(historyRepository).save(any(ApplicationStatusHistory.class));
        verify(notificationService).sendNotification(eq(recruiter), eq(student), anyString(), anyString(), anyString(), eq(NotificationType.APPLICATION_UPDATE));
    }

    @Test
    @DisplayName("Should prevent duplicate applications for the same job posting")
    void testApplyForJob_DuplicatePrevention() {
        User student = new User("student@campus.edu", "pass", "Rahul", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        Company company = new Company("Google", "IT", "https://google.com", "Desc", "Bangalore", CompanyStatus.APPROVED, null);
        Job job = new Job(company, null, "SDE-1", "Desc", JobType.FULL_TIME, "Bangalore", "20 LPA",
                new BigDecimal("8.00"), "MCA", "Java", 5, LocalDate.now().plusDays(20));

        when(jobService.getJobById(1L)).thenReturn(job);
        when(applicationRepository.existsByJobAndStudent(job, student)).thenReturn(true);

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                applicationService.applyForJob(1L, "Cover", student));

        assertTrue(ex.getMessage().contains("already submitted"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject application if student has not uploaded a resume")
    void testApplyForJob_MissingResume() {
        User student = new User("student@campus.edu", "pass", "Rahul", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        Company company = new Company("Google", "IT", "https://google.com", "Desc", "Bangalore", CompanyStatus.APPROVED, null);
        Job job = new Job(company, null, "SDE-1", "Desc", JobType.FULL_TIME, "Bangalore", "20 LPA",
                new BigDecimal("8.00"), "MCA", "Java", 5, LocalDate.now().plusDays(20));

        when(jobService.getJobById(1L)).thenReturn(job);
        when(applicationRepository.existsByJobAndStudent(job, student)).thenReturn(false);
        when(resumeStorageService.getLatestResumeForStudent(student)).thenReturn(Optional.empty());

        InvalidOperationException ex = assertThrows(InvalidOperationException.class, () ->
                applicationService.applyForJob(1L, "Cover", student));

        assertTrue(ex.getMessage().contains("upload your resume"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should bookmark and unsave jobs correctly for students")
    void testSaveAndUnsaveJob() {
        User student = new User("student@campus.edu", "pass", "Student", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        Job job = new Job(null, null, "Title", "Desc", JobType.FULL_TIME, "Loc", "Salary",
                BigDecimal.ZERO, null, null, 1, LocalDate.now().plusDays(10));
        job.setId(5L);

        when(jobService.getJobById(5L)).thenReturn(job);
        when(savedJobRepository.existsByStudentAndJob(student, job)).thenReturn(false);

        applicationService.saveJob(5L, student);
        verify(savedJobRepository).save(any(SavedJob.class));

        applicationService.unsaveJob(5L, student);
        verify(savedJobRepository).deleteByStudentAndJob(student, job);
    }

    @Test
    @DisplayName("Should successfully transition application status and record audit log")
    void testUpdateStatus_Success() {
        User student = new User("student@campus.edu", "pass", "Student", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        User recruiter = new User("recruiter@google.com", "pass", "Recruiter", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        recruiter.setId(50L);

        Job job = new Job(null, recruiter, "SDE", "Desc", JobType.FULL_TIME, "Loc", "Salary",
                BigDecimal.ZERO, null, null, 1, LocalDate.now().plusDays(10));
        Application application = new Application(job, student, null, "Cover");
        application.setId(10L);
        application.setStatus(ApplicationStatus.APPLIED);

        when(applicationRepository.findById(10L)).thenReturn(Optional.of(application));

        ApplicationStatusUpdateDto dto = new ApplicationStatusUpdateDto(ApplicationStatus.SHORTLISTED, "Strong technical portfolio");
        applicationService.updateStatus(10L, dto, recruiter);

        assertEquals(ApplicationStatus.SHORTLISTED, application.getStatus());
        verify(applicationRepository).save(application);
        verify(historyRepository).save(any(ApplicationStatusHistory.class));
        verify(notificationService).sendNotification(eq(student), eq(recruiter), anyString(), anyString(), anyString(), eq(NotificationType.APPLICATION_UPDATE));
    }
}
