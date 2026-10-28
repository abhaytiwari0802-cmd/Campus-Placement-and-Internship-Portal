package com.portal.service;

import com.portal.dto.CompanyDto;
import com.portal.dto.RecruiterProfileDto;
import com.portal.dto.RecruiterRegisterDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.*;
import com.portal.repository.RecruiterProfileRepository;
import com.portal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruiterService {

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyService companyService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public RecruiterService(UserRepository userRepository,
                            RecruiterProfileRepository recruiterProfileRepository,
                            CompanyService companyService,
                            PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.companyService = companyService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public User registerRecruiter(RecruiterRegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("Passwords do not match.");
        }
        if (userRepository.existsByEmail(dto.getEmail().toLowerCase().trim())) {
            throw new InvalidOperationException("An account with this email address already exists.");
        }

        User user = new User(
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName().trim(),
                dto.getPhone(),
                Role.ROLE_RECRUITER,
                AccountStatus.ACTIVE
        );
        User savedUser = userRepository.save(user);

        // Find or create company
        CompanyDto compDto = new CompanyDto();
        compDto.setName(dto.getCompanyName().trim());
        compDto.setIndustry(dto.getIndustry());
        compDto.setWebsite(dto.getWebsite());
        compDto.setLocation(dto.getCompanyLocation());
        compDto.setDescription(dto.getCompanyDescription());
        Company company = companyService.createOrFindCompany(compDto, savedUser);

        RecruiterProfile profile = new RecruiterProfile(
                savedUser,
                company,
                dto.getDesignation().trim(),
                dto.getDepartment(),
                dto.getPhone()
        );
        recruiterProfileRepository.save(profile);

        auditLogService.log(savedUser, "REGISTER_RECRUITER", "User", savedUser.getId(),
                "New recruiter registered for company " + company.getName(), "127.0.0.1");

        return savedUser;
    }

    @Transactional(readOnly = true)
    public RecruiterProfile getProfileByUser(User user) {
        return recruiterProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for user: " + user.getEmail()));
    }

    @Transactional
    public RecruiterProfile updateProfile(User user, RecruiterProfileDto dto) {
        RecruiterProfile profile = getProfileByUser(user);

        user.setFullName(dto.getFullName().trim());
        user.setPhone(dto.getPhone());
        userRepository.save(user);

        profile.setDesignation(dto.getDesignation().trim());
        profile.setDepartment(dto.getDepartment());
        profile.setOfficialPhone(dto.getOfficialPhone());

        RecruiterProfile updated = recruiterProfileRepository.save(profile);

        auditLogService.log(user, "UPDATE_RECRUITER_PROFILE", "RecruiterProfile", updated.getId(),
                "Updated recruiter profile for " + user.getEmail(), "127.0.0.1");

        return updated;
    }
}
