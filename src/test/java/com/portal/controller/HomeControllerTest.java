package com.portal.controller;

import com.portal.model.Company;
import com.portal.model.CompanyStatus;
import com.portal.model.JobStatus;
import com.portal.model.Role;
import com.portal.repository.CompanyRepository;
import com.portal.repository.JobRepository;
import com.portal.repository.UserRepository;
import com.portal.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private AnnouncementService announcementService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HomeController homeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    @DisplayName("GET / should display home page with live database statistics")
    void testIndex() throws Exception {
        when(announcementService.getActiveAnnouncements()).thenReturn(Collections.emptyList());
        when(companyRepository.findByApprovalStatusOrderByNameAsc(CompanyStatus.APPROVED)).thenReturn(Collections.emptyList());
        when(userRepository.countByRole(Role.ROLE_STUDENT)).thenReturn(100L);
        when(companyRepository.countByApprovalStatus(CompanyStatus.APPROVED)).thenReturn(25L);
        when(jobRepository.countByStatus(JobStatus.ACTIVE)).thenReturn(15L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalStudents", 100L))
                .andExpect(model().attribute("totalCompanies", 25L))
                .andExpect(model().attribute("totalActiveJobs", 15L));
    }

    @Test
    @DisplayName("GET /about should return about view")
    void testAbout() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    @DisplayName("GET /contact should return contact view")
    void testContact() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }
}
