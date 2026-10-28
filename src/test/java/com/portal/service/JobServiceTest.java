package com.portal.service;

import com.portal.dto.JobDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.*;
import com.portal.repository.CompanyRepository;
import com.portal.repository.JobRepository;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RecruiterService recruiterService;

    @Mock
    private AuditLogService auditLogService;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepository, companyRepository, recruiterService, auditLogService);
    }

    @Test
    @DisplayName("Should successfully create a job when recruiter has an approved company")
    void testCreateJob_Success() {
        User recruiter = new User("recruiter@google.com", "pass", "Sundar", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        recruiter.setId(10L);

        Company company = new Company("Google", "IT", "https://google.com", "Tech", "Bangalore", CompanyStatus.APPROVED, recruiter);
        company.setId(1L);

        RecruiterProfile profile = new RecruiterProfile(recruiter, company, "Lead", "Campus", "123");
        when(recruiterService.getProfileByUser(recruiter)).thenReturn(profile);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobDto dto = new JobDto();
        dto.setTitle("Software Engineer");
        dto.setDescription("Core development role");
        dto.setJobType(JobType.FULL_TIME);
        dto.setLocation("Bangalore");
        dto.setSalaryOrStipend("20 LPA");
        dto.setMinCgpa(new BigDecimal("8.00"));
        dto.setOpenings(5);
        dto.setDeadline(LocalDate.now().plusDays(30));

        Job created = jobService.createJob(dto, recruiter);

        assertNotNull(created);
        assertEquals("Software Engineer", created.getTitle());
        assertEquals(company, created.getCompany());
        assertEquals(recruiter, created.getRecruiter());
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    @DisplayName("Should reject job creation when company is pending accreditation")
    void testCreateJob_CompanyPending() {
        User recruiter = new User("recruiter@startup.com", "pass", "John", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        Company unapprovedCompany = new Company("Startup", "IT", "https://startup.com", "Tech", "Noida", CompanyStatus.PENDING, recruiter);
        RecruiterProfile profile = new RecruiterProfile(recruiter, unapprovedCompany, "Lead", "HR", "123");

        when(recruiterService.getProfileByUser(recruiter)).thenReturn(profile);

        JobDto dto = new JobDto();
        dto.setTitle("Intern");
        dto.setDescription("Internship");

        assertThrows(InvalidOperationException.class, () -> jobService.createJob(dto, recruiter));
        verify(jobRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should correctly evaluate student eligibility by CGPA and branch")
    void testIsStudentEligible() {
        User studentUser = new User("student@campus.edu", "pass", "Student", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        StudentProfile studentProfile = new StudentProfile(studentUser, "MCA/001", "MCA", "Computer Science", 2026, new BigDecimal("8.50"));

        Company company = new Company("Google", "IT", "https://google.com", "Tech", "Bangalore", CompanyStatus.APPROVED, null);
        Job job = new Job(company, null, "SDE", "Desc", JobType.FULL_TIME, "Bangalore", "20 LPA",
                new BigDecimal("8.00"), "MCA, B.Tech CSE", "Java", 5, LocalDate.now().plusDays(30));

        // Eligible
        assertTrue(jobService.isStudentEligible(studentProfile, job));

        // Ineligible due to lower CGPA
        studentProfile.setCgpa(new BigDecimal("7.50"));
        assertFalse(jobService.isStudentEligible(studentProfile, job));

        // Ineligible due to branch mismatch
        studentProfile.setCgpa(new BigDecimal("9.00"));
        studentProfile.setCourse("MBA");
        studentProfile.setBranch("Marketing");
        assertFalse(jobService.isStudentEligible(studentProfile, job));
    }

    @Test
    @DisplayName("Should prevent unauthorized recruiters from modifying another recruiter's job")
    void testUpdateJob_UnauthorizedAccess() {
        User ownerRecruiter = new User("owner@company.com", "pass", "Owner", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        ownerRecruiter.setId(1L);

        User attackerRecruiter = new User("attacker@other.com", "pass", "Attacker", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        attackerRecruiter.setId(2L);

        Company company = new Company("Company", "IT", "https://c.com", "Desc", "Delhi", CompanyStatus.APPROVED, ownerRecruiter);
        Job job = new Job(company, ownerRecruiter, "Original Title", "Desc", JobType.FULL_TIME, "Delhi", "10 LPA",
                new BigDecimal("7.0"), "MCA", "Java", 2, LocalDate.now().plusDays(10));
        job.setId(100L);

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        JobDto updateDto = new JobDto();
        updateDto.setTitle("Hacked Title");

        assertThrows(UnauthorizedAccessException.class, () -> jobService.updateJob(100L, updateDto, attackerRecruiter));
        verify(jobRepository, never()).save(any());
    }
}
