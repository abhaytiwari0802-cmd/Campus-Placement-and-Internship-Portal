package com.portal.controller;

import com.portal.model.AccountStatus;
import com.portal.model.CompanyStatus;
import com.portal.model.Role;
import com.portal.model.User;
import com.portal.repository.*;
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
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private RecruiterProfileRepository recruiterProfileRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private UserService userService;
    @Mock
    private CompanyService companyService;
    @Mock
    private JobService jobService;
    @Mock
    private AnnouncementService announcementService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ReportService reportService;
    @Mock
    private DiagnosticService diagnosticService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private User adminUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        adminUser = new User("admin@campus.edu", "pass", "Admin", "123", Role.ROLE_ADMIN, AccountStatus.ACTIVE);
        adminUser.setId(99L);
        userDetails = new CustomUserDetails(adminUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /admin/dashboard should load system statistics")
    void testDashboard() throws Exception {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByRole(Role.ROLE_STUDENT)).thenReturn(40L);
        when(userRepository.countByRole(Role.ROLE_RECRUITER)).thenReturn(9L);
        when(companyRepository.count()).thenReturn(10L);
        when(companyRepository.countByApprovalStatus(CompanyStatus.PENDING)).thenReturn(2L);
        when(jobRepository.countByStatus(any())).thenReturn(6L);
        when(applicationRepository.count()).thenReturn(80L);
        when(interviewRepository.count()).thenReturn(20L);
        when(auditLogService.getRecentLogs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/dashboard").principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalStudents", 40L))
                .andExpect(model().attribute("pendingCompanies", 2L));
    }

    @Test
    @DisplayName("POST /admin/companies/{id}/status should update accreditation and redirect")
    void testUpdateCompanyStatus() throws Exception {
        mockMvc.perform(post("/admin/companies/1/status")
                        .param("status", "APPROVED")
                        .principal(new UsernamePasswordAuthenticationToken(userDetails, null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/companies"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(companyService).updateApprovalStatus(eq(1L), eq(CompanyStatus.APPROVED), any(User.class));
    }

    @Test
    @DisplayName("GET /admin/health should display diagnostic health data")
    void testViewHealth() throws Exception {
        when(diagnosticService.getSystemHealth()).thenReturn(Map.of("Database Status", "ONLINE", "Java Version", "26"));

        mockMvc.perform(get("/admin/health"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/health"))
                .andExpect(model().attributeExists("health"));
    }
}
