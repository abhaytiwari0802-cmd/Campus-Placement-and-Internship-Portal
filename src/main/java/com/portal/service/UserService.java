package com.portal.service;

import com.portal.dto.PasswordResetConfirmDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.*;
import com.portal.repository.PasswordResetTokenRepository;
import com.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository,
                       PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateUserStatus(Long userId, AccountStatus newStatus, User adminUser) {
        User user = findById(userId);
        if (user.getRole() == Role.ROLE_ADMIN && newStatus == AccountStatus.SUSPENDED) {
            throw new InvalidOperationException("Cannot suspend an administrator account.");
        }
        AccountStatus oldStatus = user.getStatus();
        user.setStatus(newStatus);
        userRepository.save(user);

        auditLogService.log(adminUser, "UPDATE_USER_STATUS", "User", user.getId(),
                "Changed status from " + oldStatus + " to " + newStatus + " for user " + user.getEmail(), "127.0.0.1");
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = findByEmail(email);
        // Invalidate any existing unused token
        tokenRepository.findByUserAndIsUsedFalse(user).ifPresent(t -> {
            t.setIsUsed(true);
            tokenRepository.save(t);
        });

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(2);
        PasswordResetToken resetToken = new PasswordResetToken(user, token, expiryDate);
        tokenRepository.save(resetToken);

        auditLogService.log(user, "REQUEST_PASSWORD_RESET", "PasswordResetToken", resetToken.getId(),
                "Password reset requested for " + user.getEmail(), "127.0.0.1");

        return token;
    }

    @Transactional(readOnly = true)
    public PasswordResetToken validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidOperationException("Invalid password reset token."));

        if (resetToken.getIsUsed()) {
            throw new InvalidOperationException("This password reset token has already been used.");
        }

        if (resetToken.isExpired()) {
            throw new InvalidOperationException("This password reset token has expired.");
        }

        return resetToken;
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("New passwords do not match.");
        }

        PasswordResetToken resetToken = validatePasswordResetToken(dto.getToken());
        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        tokenRepository.save(resetToken);

        auditLogService.log(user, "RESET_PASSWORD_SUCCESS", "User", user.getId(),
                "Password successfully reset via token for " + user.getEmail(), "127.0.0.1");
    }

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidOperationException("New passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogService.log(user, "CHANGE_PASSWORD", "User", user.getId(),
                "User changed password directly", "127.0.0.1");
    }
}
