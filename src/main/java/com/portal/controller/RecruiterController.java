package com.portal.controller;

import com.portal.dto.*;
import com.portal.exception.InvalidOperationException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.InterviewRepository;
import com.portal.repository.JobRepository;
import com.portal.security.CustomUserDetails;
import com.portal.service.*;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/recruiter")
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final CompanyService companyService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final ResumeStorageService resumeStorageService;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public RecruiterController(RecruiterService recruiterService,
                               CompanyService companyService,
                               JobService jobService,
                               ApplicationService applicationService,
                               InterviewService interviewService,
                               ResumeStorageService resumeStorageService,
                               JobRepository jobRepository,
                               ApplicationRepository applicationRepository,
                               InterviewRepository interviewRepository) {
        this.recruiterService = recruiterService;
        this.companyService = companyService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.resumeStorageService = resumeStorageService;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User recruiter = userDetails.getUser();
        RecruiterProfile profile = recruiterService.getProfileByUser(recruiter);

        long activeJobsCount = jobRepository.findByRecruiter(recruiter).stream()
                .filter(j -> j.getStatus() == JobStatus.ACTIVE).count();
        long totalApplicantsCount = applicationRepository.countByRecruiter(recruiter);
        long shortlistedCount = applicationRepository.countByRecruiterAndStatus(recruiter, ApplicationStatus.SHORTLISTED);
        long interviewsCount = interviewRepository.countByRecruiter(recruiter);

        List<Job> myJobs = jobService.getJobsByRecruiter(recruiter);
        List<Application> recentApplications = applicationService.getRecruiterApplications(recruiter).stream().limit(5).toList();

        model.addAttribute("profile", profile);
        model.addAttribute("activeJobsCount", activeJobsCount);
        model.addAttribute("totalApplicantsCount", totalApplicantsCount);
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("interviewsCount", interviewsCount);
        model.addAttribute("myJobs", myJobs);
        model.addAttribute("recentApplications", recentApplications);

        return "recruiter/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User recruiter = userDetails.getUser();
        RecruiterProfile profile = recruiterService.getProfileByUser(recruiter);

        RecruiterProfileDto dto = new RecruiterProfileDto();
        dto.setId(profile.getId());
        dto.setFullName(recruiter.getFullName());
        dto.setEmail(recruiter.getEmail());
        dto.setPhone(recruiter.getPhone());
        dto.setDesignation(profile.getDesignation());
        dto.setDepartment(profile.getDepartment());
        dto.setOfficialPhone(profile.getOfficialPhone());
        if (profile.getCompany() != null) {
            dto.setCompanyId(profile.getCompany().getId());
            dto.setCompanyName(profile.getCompany().getName());
            dto.setCompanyStatus(profile.getCompany().getApprovalStatus().name());
        }

        model.addAttribute("profileDto", dto);
        model.addAttribute("profile", profile);

        return "recruiter/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profileDto") RecruiterProfileDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            User recruiter = userDetails.getUser();
            RecruiterProfile profile = recruiterService.getProfileByUser(recruiter);
            model.addAttribute("profile", profile);
            return "recruiter/profile";
        }

        recruiterService.updateProfile(userDetails.getUser(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/recruiter/profile";
    }

    @GetMapping("/company")
    public String viewCompany(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        RecruiterProfile profile = recruiterService.getProfileByUser(userDetails.getUser());
        Company company = profile.getCompany();

        CompanyDto dto = new CompanyDto();
        if (company != null) {
            dto.setId(company.getId());
            dto.setName(company.getName());
            dto.setIndustry(company.getIndustry());
            dto.setWebsite(company.getWebsite());
            dto.setDescription(company.getDescription());
            dto.setLocation(company.getLocation());
            dto.setLogoUrl(company.getLogoUrl());
            dto.setApprovalStatus(company.getApprovalStatus().name());
        }

        model.addAttribute("company", company);
        model.addAttribute("companyDto", dto);

        return "recruiter/company";
    }

    @PostMapping("/company/update")
    public String updateCompany(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("companyDto") CompanyDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            RecruiterProfile profile = recruiterService.getProfileByUser(userDetails.getUser());
            model.addAttribute("company", profile.getCompany());
            return "recruiter/company";
        }

        RecruiterProfile profile = recruiterService.getProfileByUser(userDetails.getUser());
        if (profile.getCompany() != null) {
            companyService.updateCompany(profile.getCompany().getId(), dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Company information updated successfully!");
        }

        return "redirect:/recruiter/company";
    }

    @GetMapping("/jobs")
    public String listJobs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User recruiter = userDetails.getUser();
        List<Job> jobs = jobService.getJobsByRecruiter(recruiter);
        model.addAttribute("jobs", jobs);
        return "recruiter/jobs";
    }

    @GetMapping("/jobs/create")
    public String showCreateJobForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        RecruiterProfile profile = recruiterService.getProfileByUser(userDetails.getUser());
        if (profile.getCompany() == null || profile.getCompany().getApprovalStatus() != CompanyStatus.APPROVED) {
            model.addAttribute("companyNotApproved", true);
            model.addAttribute("company", profile.getCompany());
        }

        if (!model.containsAttribute("jobDto")) {
            JobDto dto = new JobDto();
            dto.setCompanyId(profile.getCompany() != null ? profile.getCompany().getId() : null);
            dto.setCompanyName(profile.getCompany() != null ? profile.getCompany().getName() : "Unassigned");
            model.addAttribute("jobDto", dto);
        }

        return "recruiter/job-create";
    }

    @PostMapping("/jobs/create")
    public String processCreateJob(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @Valid @ModelAttribute("jobDto") JobDto dto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            return "recruiter/job-create";
        }
        try {
            jobService.createJob(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Job opportunity posted successfully!");
            return "redirect:/recruiter/jobs";
        } catch (InvalidOperationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "recruiter/job-create";
        }
    }

    @GetMapping("/jobs/{id}/edit")
    public String showEditJobForm(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        Job job = jobService.getJobById(id);

        JobDto dto = new JobDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setJobType(job.getJobType());
        dto.setLocation(job.getLocation());
        dto.setSalaryOrStipend(job.getSalaryOrStipend());
        dto.setMinCgpa(job.getMinCgpa());
        dto.setAllowedBranches(job.getAllowedBranches());
        dto.setRequiredSkills(job.getRequiredSkills());
        dto.setOpenings(job.getOpenings());
        dto.setDeadline(job.getDeadline());
        dto.setStatus(job.getStatus());
        dto.setCompanyId(job.getCompany().getId());
        dto.setCompanyName(job.getCompany().getName());

        model.addAttribute("jobDto", dto);
        model.addAttribute("job", job);

        return "recruiter/job-edit";
    }

    @PostMapping("/jobs/{id}/edit")
    public String processEditJob(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("jobDto") JobDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            dto.setId(id);
            return "recruiter/job-edit";
        }
        try {
            jobService.updateJob(id, dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Job posting updated successfully!");
            return "redirect:/recruiter/jobs";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "recruiter/job-edit";
        }
    }

    @PostMapping("/jobs/{id}/toggle-status")
    public String toggleJobStatus(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        jobService.toggleJobStatus(id, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Job status updated successfully.");
        return "redirect:/recruiter/jobs";
    }

    @GetMapping("/jobs/{id}/applicants")
    public String viewApplicants(@PathVariable("id") Long jobId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Job job = jobService.getJobById(jobId);
        List<Application> applicants = applicationService.getJobApplications(jobId, userDetails.getUser());

        model.addAttribute("job", job);
        model.addAttribute("applicants", applicants);

        return "recruiter/job-applicants";
    }

    @GetMapping("/applications/{id}")
    public String viewApplicationDetails(@PathVariable("id") Long applicationId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        Application application = applicationService.getApplicationById(applicationId);
        List<ApplicationStatusHistory> history = applicationService.getApplicationHistory(application);

        model.addAttribute("application", application);
        model.addAttribute("history", history);
        model.addAttribute("statusUpdateDto", new ApplicationStatusUpdateDto());

        InterviewScheduleDto scheduleDto = new InterviewScheduleDto();
        scheduleDto.setApplicationId(application.getId());
        model.addAttribute("scheduleDto", scheduleDto);

        return "recruiter/application-details";
    }

    @PostMapping("/applications/{id}/update-status")
    public String updateApplicationStatus(@PathVariable("id") Long applicationId,
                                          @Valid @ModelAttribute("statusUpdateDto") ApplicationStatusUpdateDto dto,
                                          @AuthenticationPrincipal CustomUserDetails userDetails,
                                          RedirectAttributes redirectAttributes) {
        try {
            applicationService.updateStatus(applicationId, dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Application status updated successfully to: " + dto.getStatus().getLabel());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/recruiter/applications/" + applicationId;
    }

    @PostMapping("/interviews/schedule")
    public String scheduleInterview(@Valid @ModelAttribute("scheduleDto") InterviewScheduleDto dto,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid interview schedule parameters. Please check date and time format.");
            return "redirect:/recruiter/applications/" + dto.getApplicationId();
        }
        try {
            interviewService.scheduleInterview(dto, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Interview scheduled and candidate notified successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/recruiter/applications/" + dto.getApplicationId();
    }

    @GetMapping("/interviews")
    public String listInterviews(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User recruiter = userDetails.getUser();
        List<Interview> interviews = interviewService.getInterviewsByRecruiter(recruiter);
        model.addAttribute("interviews", interviews);
        return "recruiter/interviews";
    }

    @GetMapping("/resumes/{id}/download")
    public ResponseEntity<Resource> downloadCandidateResume(@PathVariable("id") Long resumeId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Resource resource = resumeStorageService.loadFileAsResource(resumeId, userDetails.getUser());
        Resume resume = resumeStorageService.getResumeById(resumeId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                .body(resource);
    }
}
