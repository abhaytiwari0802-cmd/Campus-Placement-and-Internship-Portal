package com.portal.service;

import com.portal.dto.StudentProfileDto;
import com.portal.dto.StudentRegisterDto;
import com.portal.exception.InvalidOperationException;
import com.portal.model.*;
import com.portal.repository.StudentProfileRepository;
import com.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(userRepository, studentProfileRepository, passwordEncoder, auditLogService);
    }

    @Test
    @DisplayName("Should successfully register a new student and create academic profile")
    void testRegisterStudent_Success() {
        StudentRegisterDto dto = new StudentRegisterDto();
        dto.setEmail("student.new@campus.edu");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setFullName("Rahul Sharma");
        dto.setPhone("+91 9876543210");
        dto.setEnrollmentNumber("MCA/2024/099");
        dto.setCourse("MCA");
        dto.setBranch("Computer Science");
        dto.setGraduationYear(2026);
        dto.setCgpa(new BigDecimal("8.75"));

        when(userRepository.existsByEmail("student.new@campus.edu")).thenReturn(false);
        when(studentProfileRepository.existsByEnrollmentNumber("MCA/2024/099")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = studentService.registerStudent(dto);

        assertNotNull(result);
        assertEquals("student.new@campus.edu", result.getEmail());
        assertEquals(Role.ROLE_STUDENT, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    void testRegisterStudent_PasswordMismatch() {
        StudentRegisterDto dto = new StudentRegisterDto();
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("DifferentPassword");

        assertThrows(InvalidOperationException.class, () -> studentService.registerStudent(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration when email is already in use")
    void testRegisterStudent_DuplicateEmail() {
        StudentRegisterDto dto = new StudentRegisterDto();
        dto.setEmail("existing@campus.edu");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");

        when(userRepository.existsByEmail("existing@campus.edu")).thenReturn(true);

        assertThrows(InvalidOperationException.class, () -> studentService.registerStudent(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject registration when enrollment number already exists")
    void testRegisterStudent_DuplicateEnrollment() {
        StudentRegisterDto dto = new StudentRegisterDto();
        dto.setEmail("new@campus.edu");
        dto.setPassword("Secret@123");
        dto.setConfirmPassword("Secret@123");
        dto.setEnrollmentNumber("MCA/2024/001");

        when(userRepository.existsByEmail("new@campus.edu")).thenReturn(false);
        when(studentProfileRepository.existsByEnrollmentNumber("MCA/2024/001")).thenReturn(true);

        assertThrows(InvalidOperationException.class, () -> studentService.registerStudent(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update student profile details and recalculate profile strength")
    void testUpdateProfile_Success() {
        User user = new User("student@campus.edu", "pass", "Old Name", "123", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        StudentProfile profile = new StudentProfile(user, "MCA/001", "MCA", "CS", 2026, new BigDecimal("8.00"));

        when(studentProfileRepository.findByUser(user)).thenReturn(Optional.of(profile));

        StudentProfileDto updateDto = new StudentProfileDto();
        updateDto.setFullName("Updated Full Name");
        updateDto.setPhone("+91 9999988888");
        updateDto.setCourse("MCA");
        updateDto.setBranch("Computer Science & Engineering");
        updateDto.setGraduationYear(2026);
        updateDto.setCgpa(new BigDecimal("9.10"));
        updateDto.setSkills("Java, Spring Boot, MySQL, Docker");
        updateDto.setProjects("Placement Portal");
        updateDto.setCertifications("Oracle Certified");
        updateDto.setLinkedinUrl("https://linkedin.com/in/student");

        studentService.updateProfile(user, updateDto);

        assertEquals("Updated Full Name", user.getFullName());
        assertEquals("+91 9999988888", user.getPhone());
        assertEquals("Java, Spring Boot, MySQL, Docker", profile.getSkills());
        assertTrue(profile.getProfileCompletion() >= 80);
        verify(userRepository).save(user);
        verify(studentProfileRepository).save(profile);
    }
}
