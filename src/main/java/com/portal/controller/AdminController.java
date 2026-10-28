package com.portal.controller;

import com.portal.dto.AnnouncementDto;
import com.portal.dto.PlacementReportDto;
import com.portal.model.*;
import com.portal.repository.*;
import com.portal.security.CustomUserDetails;
import com.portal.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final UserService userService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final AnnouncementService announcementService;
    private final AuditLogService auditLogService;
    private final ReportService reportService;
    private final DiagnosticService diagnosticService;

    public AdminController(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           RecruiterProfileRepository recruiterProfileRepository,
                           CompanyRepository companyRepository,
                           JobRepository jobRepository,
                           ApplicationRepository applicationRepository,
                           InterviewRepository interviewRepository,
                           UserService userService,
                           CompanyService companyService,
                           JobService jobService,
                           AnnouncementService announcementService,
                           AuditLogService auditLogService,
                           ReportService reportService,
                           DiagnosticService diagnosticService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.userService = userService;
        this.companyService = companyService;
        this.jobService = jobService;
        this.announcementService = announcementService;
        this.auditLogService = auditLogService;
        this.reportService = reportService;
        this.diagnosticService = diagnosticService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalStudents", userRepository.countByRole(Role.ROLE_STUDENT));
        model.addAttribute("totalRecruiters", userRepository.countByRole(Role.ROLE_RECRUITER));
        model.addAttribute("totalCompanies", companyRepository.count());
        model.addAttribute("pendingCompanies", companyRepository.countByApprovalStatus(CompanyStatus.PENDING));
        model.addAttribute("totalActiveJobs", jobRepository.countByStatus(JobStatus.ACTIVE));
        model.addAttribute("totalApplications", applicationRepository.count());
        model.addAttribute("totalInterviews", interviewRepository.count());
        model.addAttribute("recentLogs", auditLogService.getRecentLogs().stream().limit(8).toList());

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/status")
    public String changeUserStatus(@PathVariable("id") Long userId,
                                   @RequestParam("status") AccountStatus status,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserStatus(userId, status, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "User account status updated to " + status);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/companies")
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/companies";
    }

    @PostMapping("/companies/{id}/status")
    public String updateCompanyStatus(@PathVariable("id") Long companyId,
                                      @RequestParam("status") CompanyStatus status,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        companyService.updateApprovalStatus(companyId, status, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Company verification status updated to " + status);
        return "redirect:/admin/companies";
    }

    @GetMapping("/jobs")
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobService.getAllJobs());
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/toggle-status")
    public String toggleJobStatus(@PathVariable("id") Long jobId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        jobService.toggleJobStatus(jobId, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Job status toggled successfully.");
        return "redirect:/admin/jobs";
    }

    @GetMapping("/announcements")
    public String listAnnouncements(Model model) {
        model.addAttribute("announcements", announcementService.getAllAnnouncements());
        model.addAttribute("announcementDto", new AnnouncementDto());
        return "admin/announcements";
    }

    @PostMapping("/announcements")
    public String createAnnouncement(@Valid @ModelAttribute("announcementDto") AnnouncementDto dto,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("announcements", announcementService.getAllAnnouncements());
            return "admin/announcements";
        }
        announcementService.createAnnouncement(dto, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Campus announcement broadcasted successfully!");
        return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/toggle")
    public String toggleAnnouncement(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        announcementService.toggleActiveStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Announcement visibility toggled.");
        return "redirect:/admin/announcements";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        announcementService.deleteAnnouncement(id);
        redirectAttributes.addFlashAttribute("successMessage", "Announcement removed.");
        return "redirect:/admin/announcements";
    }

    @GetMapping("/reports")
    public String viewReports(Model model) {
        List<PlacementReportDto> companyReports = reportService.getCompanyPlacementReport();
        List<Map<String, Object>> branchStats = reportService.getBranchPlacementStatistics();
        Map<String, Long> statusBreakdown = reportService.getApplicationStatusDistribution();

        model.addAttribute("companyReports", companyReports);
        model.addAttribute("branchStats", branchStats);
        model.addAttribute("statusBreakdown", statusBreakdown);

        return "admin/reports";
    }

    @GetMapping("/audit-logs")
    public String viewAuditLogs(Model model) {
        model.addAttribute("logs", auditLogService.getRecentLogs());
        return "admin/audit-logs";
    }

    @GetMapping("/health")
    public String viewHealth(Model model) {
        model.addAttribute("health", diagnosticService.getSystemHealth());
        return "admin/health";
    }
}
