package com.portal.controller;

import com.portal.dto.StudentRegisterDto;
import com.portal.model.AccountStatus;
import com.portal.model.Role;
import com.portal.model.User;
import com.portal.service.RecruiterService;
import com.portal.service.StudentService;
import com.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private StudentService studentService;

    @Mock
    private RecruiterService recruiterService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("GET /login should display login view")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /register should display registration choice view")
    void testRegisterChoice() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-choice"));
    }

    @Test
    @DisplayName("GET /register/student should display student registration form")
    void testShowStudentRegisterForm() throws Exception {
        mockMvc.perform(get("/register/student"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-student"))
                .andExpect(model().attributeExists("studentDto"));
    }

    @Test
    @DisplayName("POST /register/student with valid data should redirect to login")
    void testProcessStudentRegistration_Success() throws Exception {
        User registeredUser = new User("rahul@campus.edu", "pass", "Rahul Sharma", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        when(studentService.registerStudent(any(StudentRegisterDto.class))).thenReturn(registeredUser);

        mockMvc.perform(post("/register/student")
                        .param("fullName", "Rahul Sharma")
                        .param("email", "rahul@campus.edu")
                        .param("password", "Secret@123")
                        .param("confirmPassword", "Secret@123")
                        .param("enrollmentNumber", "MCA/2024/001")
                        .param("course", "MCA")
                        .param("branch", "Computer Science")
                        .param("graduationYear", "2026")
                        .param("cgpa", "8.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(studentService).registerStudent(any(StudentRegisterDto.class));
    }

    @Test
    @DisplayName("GET /forgot-password should display reset request form")
    void testShowForgotPasswordForm() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/forgot-password"))
                .andExpect(model().attributeExists("resetRequestDto"));
    }
}
