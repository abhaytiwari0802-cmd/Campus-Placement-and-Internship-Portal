package com.portal.service;

import com.portal.dto.PasswordResetConfirmDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.*;
import com.portal.repository.PasswordResetTokenRepository;
import com.portal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, tokenRepository, passwordEncoder, auditLogService);
    }

    @Test
    @DisplayName("Should create password reset token for valid active user")
    void testCreatePasswordResetToken_Success() {
        User user = new User("student@campus.edu", "encodedPass", "Test Student", "1234567890", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        when(userRepository.findByEmail("student@campus.edu")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserAndIsUsedFalse(user)).thenReturn(Optional.empty());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = userService.createPasswordResetToken("student@campus.edu");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should throw exception when requesting reset token for non-existent email")
    void testCreatePasswordResetToken_UserNotFound() {
        when(userRepository.findByEmail("unknown@campus.edu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.createPasswordResetToken("unknown@campus.edu"));
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully reset password when valid token is provided")
    void testResetPassword_Success() {
        User user = new User("student@campus.edu", "oldEncoded", "Test Student", "1234567890", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        PasswordResetToken token = new PasswordResetToken("valid-token-123", user, LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("newEncoded");

        PasswordResetConfirmDto dto = new PasswordResetConfirmDto();
        dto.setToken("valid-token-123");
        dto.setNewPassword("NewPassword@123");
        dto.setConfirmPassword("NewPassword@123");

        userService.resetPassword(dto);

        assertTrue(token.isUsed());
        assertEquals("newEncoded", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("Should throw exception if token is expired during password reset")
    void testResetPassword_ExpiredToken() {
        User user = new User("student@campus.edu", "oldEncoded", "Test Student", "1234567890", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        PasswordResetToken expiredToken = new PasswordResetToken("expired-token", user, LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        PasswordResetConfirmDto dto = new PasswordResetConfirmDto();
        dto.setToken("expired-token");
        dto.setNewPassword("NewPassword@123");
        dto.setConfirmPassword("NewPassword@123");

        assertThrows(InvalidOperationException.class, () -> userService.resetPassword(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update user account status when performed by admin")
    void testUpdateUserStatus_Success() {
        User target = new User("student@campus.edu", "pass", "Student", "1234", Role.ROLE_STUDENT, AccountStatus.ACTIVE);
        User admin = new User("admin@campus.edu", "pass", "Admin", "1234", Role.ROLE_ADMIN, AccountStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(target));

        userService.updateUserStatus(1L, AccountStatus.SUSPENDED, admin);

        assertEquals(AccountStatus.SUSPENDED, target.getStatus());
        verify(userRepository).save(target);
        verify(auditLogService).log(eq(admin), eq("UPDATE_USER_STATUS"), eq("User"), eq(1L), anyString(), eq("127.0.0.1"));
    }
}
