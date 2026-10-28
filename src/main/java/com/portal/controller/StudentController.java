package com.portal.controller;

import com.portal.dto.JobSearchCriteria;
import com.portal.dto.StudentProfileDto;
import com.portal.exception.InvalidOperationException;
import com.portal.model.*;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.SavedJobRepository;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final ResumeStorageService resumeStorageService;
    private final NotificationService notificationService;
    private final ApplicationRepository applicationRepository;
    private final SavedJobRepository savedJobRepository;

    public StudentController(StudentService studentService,
                             JobService jobService,
                             ApplicationService applicationService,
                             InterviewService interviewService,
                             ResumeStorageService resumeStorageService,
                             NotificationService notificationService,
                             ApplicationRepository applicationRepository,
                             SavedJobRepository savedJobRepository) {
        this.studentService = studentService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.resumeStorageService = resumeStorageService;
        this.notificationService = notificationService;
        this.applicationRepository = applicationRepository;
        this.savedJobRepository = savedJobRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        StudentProfile profile = studentService.getProfileByUser(student);

        long totalApps = applicationRepository.countByStudent(student);
        long savedJobsCount = savedJobRepository.countByStudent(student);
        List<Interview> upcomingInterviews = interviewService.getUpcomingInterviewsForStudent(student);
        List<Notification> recentNotifications = notificationService.getRecentUserNotifications(student);
        long unreadNotifications = notificationService.getUnreadCount(student);

        // Group applications by status for visual chart / progress cards
        List<Application> studentApps = applicationService.getStudentApplications(student);
        Map<String, Long> statusDistribution = studentApps.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));

        model.addAttribute("profile", profile);
        model.addAttribute("totalApps", totalApps);
        model.addAttribute("savedJobsCount", savedJobsCount);
        model.addAttribute("upcomingInterviews", upcomingInterviews);
        model.addAttribute("recentNotifications", recentNotifications);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("statusDistribution", statusDistribution);
        model.addAttribute("recentApplications", studentApps.stream().limit(5).toList());

        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        StudentProfile profile = studentService.getProfileByUser(student);

        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(profile.getId());
        dto.setFullName(student.getFullName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setEnrollmentNumber(profile.getEnrollmentNumber());
        dto.setCourse(profile.getCourse());
        dto.setBranch(profile.getBranch());
        dto.setGraduationYear(profile.getGraduationYear());
        dto.setCgpa(profile.getCgpa());
        dto.setSkills(profile.getSkills());
        dto.setProjects(profile.getProjects());
        dto.setCertifications(profile.getCertifications());
        dto.setLinkedinUrl(profile.getLinkedinUrl());
        dto.setProfileCompletion(profile.getProfileCompletion());

        model.addAttribute("profileDto", dto);
        model.addAttribute("profile", profile);
        model.addAttribute("latestResume", resumeStorageService.getLatestResumeForStudent(student).orElse(null));

        return "student/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profileDto") StudentProfileDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            User student = userDetails.getUser();
            StudentProfile profile = studentService.getProfileByUser(student);
            model.addAttribute("profile", profile);
            model.addAttribute("latestResume", resumeStorageService.getLatestResumeForStudent(student).orElse(null));
            return "student/profile";
        }

        studentService.updateProfile(userDetails.getUser(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/student/profile";
    }

    @PostMapping("/resume/upload")
    public String uploadResume(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a valid resume file to upload.");
            return "redirect:/student/profile";
        }
        try {
            resumeStorageService.storeFile(file, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Resume uploaded and attached to profile successfully!");
        } catch (InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/profile";
    }

    @GetMapping("/resume/download/{id}")
    public ResponseEntity<Resource> downloadResume(@PathVariable("id") Long resumeId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Resource resource = resumeStorageService.loadFileAsResource(resumeId, userDetails.getUser());
        Resume resume = resumeStorageService.getResumeById(resumeId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/jobs")
    public String browseJobs(@ModelAttribute("searchCriteria") JobSearchCriteria criteria,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        List<Job> jobs = jobService.searchJobs(criteria);
        User student = userDetails.getUser();
        StudentProfile profile = studentService.getProfileByUser(student);

        model.addAttribute("jobs", jobs);
        model.addAttribute("studentProfile", profile);
        model.addAttribute("jobService", jobService);
        model.addAttribute("searchCriteria", criteria);

        return "student/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String viewJobDetails(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Job job = jobService.getJobById(id);
        User student = userDetails.getUser();
        StudentProfile profile = studentService.getProfileByUser(student);

        boolean hasApplied = applicationService.hasStudentApplied(id, student);
        boolean isSaved = applicationService.isJobSaved(id, student);
        boolean isEligible = jobService.isStudentEligible(profile, job);
        boolean hasResume = resumeStorageService.getLatestResumeForStudent(student).isPresent();

        model.addAttribute("job", job);
        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("isEligible", isEligible);
        model.addAttribute("hasResume", hasResume);
        model.addAttribute("profile", profile);

        return "student/job-details";
    }

    @PostMapping("/jobs/{id}/apply")
    public String applyForJob(@PathVariable("id") Long jobId,
                              @RequestParam(value = "coverLetter", required = false) String coverLetter,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            applicationService.applyForJob(jobId, coverLetter, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Application submitted successfully!");
            return "redirect:/student/applications";
        } catch (InvalidOperationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/jobs/" + jobId;
        }
    }

    @PostMapping("/jobs/{id}/save")
    public String saveJob(@PathVariable("id") Long jobId,
                          @AuthenticationPrincipal CustomUserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        applicationService.saveJob(jobId, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Job saved to your bookmarks.");
        return "redirect:/student/jobs/" + jobId;
    }

    @PostMapping("/jobs/{id}/unsave")
    public String unsaveJob(@PathVariable("id") Long jobId,
                            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        applicationService.unsaveJob(jobId, userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Job removed from saved list.");
        return (redirectUrl != null && !redirectUrl.isEmpty()) ? "redirect:" + redirectUrl : "redirect:/student/saved-jobs";
    }

    @GetMapping("/applications")
    public String myApplications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        List<Application> applications = applicationService.getStudentApplications(student);
        model.addAttribute("applications", applications);
        return "student/applications";
    }

    @PostMapping("/applications/{id}/withdraw")
    public String withdrawApplication(@PathVariable("id") Long id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            applicationService.withdrawApplication(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Application withdrawn successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/applications";
    }

    @GetMapping("/saved-jobs")
    public String savedJobs(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        List<SavedJob> savedJobs = applicationService.getSavedJobsForStudent(student);
        model.addAttribute("savedJobs", savedJobs);
        return "student/saved-jobs";
    }

    @GetMapping("/interviews")
    public String myInterviews(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        List<Interview> upcoming = interviewService.getUpcomingInterviewsForStudent(student);
        List<Interview> all = interviewService.getAllInterviewsForStudent(student);

        model.addAttribute("upcomingInterviews", upcoming);
        model.addAttribute("allInterviews", all);
        return "student/interviews";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User student = userDetails.getUser();
        model.addAttribute("notifications", notificationService.getUserNotifications(student));
        model.addAttribute("unreadCount", notificationService.getUnreadCount(student));
        return "student/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable("id") Long id,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUser());
        return "redirect:/student/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUser());
        return "redirect:/student/notifications";
    }
}
