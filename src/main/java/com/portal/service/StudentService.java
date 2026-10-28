package com.portal.service;

import com.portal.dto.StudentProfileDto;
import com.portal.dto.StudentRegisterDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.AccountStatus;
import com.portal.model.Role;
import com.portal.model.StudentProfile;
import com.portal.model.User;
import com.portal.repository.StudentProfileRepository;
import com.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public StudentService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          PasswordEncoder passwordEncoder,
                          AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User registerStudent(StudentRegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match.");
        }
        if (userRepository.existsByEmail(dto.getEmail().toLowerCase().trim())) {
            throw new InvalidOperationException("An account with this email address already exists.");
        }
        if (studentProfileRepository.existsByEnrollmentNumber(dto.getEnrollmentNumber().trim())) {
            throw new InvalidOperationException("A student with this enrollment number is already registered.");
        }

        User user = new User(
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName().trim(),
                dto.getPhone(),
                Role.ROLE_STUDENT,
                AccountStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        StudentProfile profile = new StudentProfile(
                savedUser,
                dto.getEnrollmentNumber().trim(),
                dto.getCourse().trim(),
                dto.getBranch().trim(),
                dto.getGraduationYear(),
                dto.getCgpa()
        );
        profile.calculateProfileCompletion();
        studentProfileRepository.save(profile);

        auditLogService.log(savedUser, "REGISTER_STUDENT", "User", savedUser.getId(),
                "New student registered: " + savedUser.getEmail(), "127.0.0.1");

        return savedUser;
    }

    @Transactional(readOnly = true)
    public StudentProfile getProfileByUser(User user) {
        return studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + user.getEmail()));
    }

    @Transactional(readOnly = true)
    public StudentProfile getProfileByUserId(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + userId));
    }

    @Transactional
    public StudentProfile updateProfile(User user, StudentProfileDto dto) {
        StudentProfile profile = getProfileByUser(user);

        // Update User fields
        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        // Update Profile fields
        profile.setCourse(dto.getCourse().trim());
        profile.setBranch(dto.getBranch().trim());
        profile.setGraduationYear(dto.getGraduationYear());
        profile.setCgpa(dto.getCgpa());
        profile.setSkills(dto.getSkills());
        profile.setProjects(dto.getProjects());
        profile.setCertifications(dto.getCertifications());
        profile.setLinkedinUrl(dto.getLinkedinUrl());
        profile.calculateProfileCompletion();

        StudentProfile updated = studentProfileRepository.save(profile);

        auditLogService.log(user, "UPDATE_STUDENT_PROFILE", "StudentProfile", updated.getId(),
                "Updated student profile for " + user.getEmail(), "127.0.0.1");

        return updated;
    }
}
