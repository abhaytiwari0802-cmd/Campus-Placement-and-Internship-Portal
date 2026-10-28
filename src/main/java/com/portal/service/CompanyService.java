package com.portal.service;

import com.portal.dto.CompanyDto;
import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.model.Company;
import com.portal.model.CompanyStatus;
import com.portal.model.NotificationType;
import com.portal.model.User;
import com.portal.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public CompanyService(CompanyRepository companyRepository,
                          NotificationService notificationService,
                          AuditLogService auditLogService) {
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<Company> getApprovedCompanies() {
        return companyRepository.findByApprovalStatusOrderByNameAsc(CompanyStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Company> getPendingCompanies() {
        return companyRepository.findByApprovalStatus(CompanyStatusStatusOrPending(CompanyStatus.PENDING));
    }

    private CompanyStatus CompanyStatusStatusOrPending(CompanyStatus pending) {
        return pending;
    }

    @Transactional(readOnly = true)
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    @Transactional
    public Company createOrFindCompany(CompanyDto dto, User recruiter) {
        return companyRepository.findByNameIgnoreCase(dto.getName())
                .orElseGet(() -> {
                    Company company = new Company(
                            dto.getName(),
                            dto.getIndustry(),
                            dto.getWebsite(),
                            dto.getDescription(),
                            dto.getLocation(),
                            CompanyStatus.PENDING,
                            recruiter
                    );
                    company.setLogoUrl(dto.getLogoUrl());
                    Company saved = companyRepository.save(company);
                    auditLogService.log(recruiter, "SUBMIT_COMPANY", "Company", saved.getId(),
                            "Submitted company " + saved.getName() + " for admin approval", "127.0.0.1");
                    return saved;
                });
    }

    @Transactional
    public void updateApprovalStatus(Long companyId, CompanyStatus newStatus, User admin) {
        Company company = getCompanyById(companyId);
        CompanyStatus oldStatus = company.getApprovalStatus();
        company.setApprovalStatus(newStatus);
        companyRepository.save(company);

        auditLogService.log(admin, "COMPANY_APPROVAL_DECISION", "Company", company.getId(),
                "Changed status from " + oldStatus + " to " + newStatus + " for " + company.getName(), "127.0.0.1");

        // Notify recruiter who created the company
        if (company.getCreatedByRecruiter() != null) {
            String title = "Company Verification Status: " + newStatus;
            String message = "Your company profile for '" + company.getName() + "' has been " + newStatus.name().toLowerCase() + " by the administrator.";
            notificationService.sendNotification(company.getCreatedByRecruiter(), admin, title, message, "/recruiter/company", NotificationType.COMPANY_APPROVAL);
        }
    }

    @Transactional
    public Company updateCompany(Long id, CompanyDto dto, User recruiter) {
        Company company = getCompanyById(id);
        company.setName(dto.getName());
        company.setIndustry(dto.getIndustry());
        company.setWebsite(dto.getWebsite());
        company.setLocation(dto.getLocation());
        company.setDescription(dto.getDescription());
        if (dto.getLogoUrl() != null && !dto.getLogoUrl().trim().isEmpty()) {
            company.setLogoUrl(dto.getLogoUrl());
        }
        return companyRepository.save(company);
    }
}
