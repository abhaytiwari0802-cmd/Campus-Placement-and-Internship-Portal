package com.portal.controller;

import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.InterviewRepository;
import com.portal.repository.JobRepository;
import com.portal.security.CustomUserDetails;
import com.portal.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecruiterControllerTest {

    @Mock
    private RecruiterService recruiterService;
    @Mock
    private CompanyService companyService;
    @Mock
    private JobService jobService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private InterviewService interviewService;
    @Mock
    private ResumeStorageService resumeStorageService;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private InterviewRepository interviewRepository;

    @InjectMocks
    private RecruiterController recruiterController;

    private MockMvc mockMvc;
    private User recruiterUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recruiterController).build();
        recruiterUser = new User("recruiter@google.com", "pass", "Sundar Raman", "123", Role.ROLE_RECRUITER, AccountStatus.ACTIVE);
        recruiterUser.setId(2L);
        userDetails = new CustomUserDetails(recruiterUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /recruiter/dashboard should populate recruiter metrics")
    void testDashboard() throws Exception {
        RecruiterProfile profile = new RecruiterProfile(recruiterUser, null, "Lead", "HR", "123");
        when(recruiterService.getProfileByUser(any(User.class))).thenReturn(profile);
        when(jobRepository.findByRecruiter(any(User.class))).thenReturn(Collections.emptyList());
        when(applicationRepository.countByRecruiter(any(User.class))).thenReturn(5L);
        when(applicationRepository.countByRecruiterAndStatus(any(User.class), eq(ApplicationStatus.SHORTLISTED))).thenReturn(2L);
        when(interviewRepository.countByRecruiter(any(User.class))).thenReturn(1L);
        when(jobService.getJobsByRecruiter(any(User.class))).thenReturn(Collections.emptyList());
        when(applicationService.getRecruiterApplications(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recruiter/dashboard").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/dashboard"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attribute("totalApplicantsCount", 5L))
                .andExpect(model().attribute("shortlistedCount", 2L));
    }

    @Test
    @DisplayName("GET /recruiter/jobs should list recruiter jobs")
    void testListJobs() throws Exception {
        when(jobService.getJobsByRecruiter(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recruiter/jobs").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().isOk())
                .andExpect(view().name("recruiter/jobs"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    @DisplayName("POST /recruiter/jobs/{id}/toggle-status should toggle status and redirect")
    void testToggleJobStatus() throws Exception {
        mockMvc.perform(post("/recruiter/jobs/10/toggle-status").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recruiter/jobs"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(jobService).toggleJobStatus(eq(10L), any(User.class));
    }
}
