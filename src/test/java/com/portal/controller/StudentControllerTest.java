package com.portal.controller;

import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.SavedJobRepository;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;
    @Mock
    private JobService jobService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private InterviewService interviewService;
    @Mock
    private ResumeStorageService resumeStorageService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private SavedJobRepository savedJobRepository;

    @InjectMocks
    private StudentController studentController;

    private MockMvc mockMvc;
    private User studentUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController).build();
        studentUser = new User("rahul@campus.edu", "pass", "Rahul Sharma", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        studentUser.setId(1L);
        userDetails = new CustomUserDetails(studentUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /student/dashboard should populate model with student metrics")
    void testDashboard() throws Exception {
        StudentProfile profile = new StudentProfile(studentUser, "MCA/001", "MCA", "CS", 2026, new BigDecimal("8.85"));
        when(studentService.getProfileByUser(any(User.class))).thenReturn(profile);
        when(applicationRepository.countByStudent(any(User.class))).thenReturn(3L);
        when(savedJobRepository.countByStudent(any(User.class))).thenReturn(2L);
        when(interviewService.getUpcomingInterviewsForStudent(any(User.class))).thenReturn(Collections.emptyList());
        when(notificationService.getRecentUserNotifications(any(User.class))).thenReturn(Collections.emptyList());
        when(notificationService.getUnreadCount(any(User.class))).thenReturn(1L);
        when(applicationService.getStudentApplications(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/dashboard").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attribute("totalApps", 3L))
                .andExpect(model().attribute("savedJobsCount", 2L));
    }

    @Test
    @DisplayName("GET /student/jobs should display job search page")
    void testBrowseJobs() throws Exception {
        StudentProfile profile = new StudentProfile(studentUser, "MCA/001", "MCA", "CS", 2026, new BigDecimal("8.85"));
        when(studentService.getProfileByUser(any(User.class))).thenReturn(profile);
        when(jobService.searchJobs(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/student/jobs").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().isOk())
                .andExpect(view().name("student/jobs"))
                .andExpect(model().attributeExists("jobs"))
                .andExpect(model().attributeExists("studentProfile"));
    }

    @Test
    @DisplayName("POST /student/jobs/{id}/save should bookmark a job")
    void testSaveJob() throws Exception {
        mockMvc.perform(post("/student/jobs/5/save").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/jobs/5"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(applicationService).saveJob(eq(5L), any(User.class));
    }

    @Test
    @DisplayName("POST /student/jobs/{id}/unsave should remove bookmarked job")
    void testUnsaveJob() throws Exception {
        mockMvc.perform(post("/student/jobs/5/unsave").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/saved-jobs"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(applicationService).unsaveJob(eq(5L), any(User.class));
    }
}
