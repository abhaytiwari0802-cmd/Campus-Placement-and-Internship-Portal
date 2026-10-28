package com.portal.controller;

import com.portal.model.CompanyStatus;
import com.portal.model.JobStatus;
import com.portal.model.Role;
import com.portal.repository.CompanyRepository;
import com.portal.repository.JobRepository;
import com.portal.repository.UserRepository;
import com.portal.service.AnnouncementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AnnouncementService announcementService;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public HomeController(AnnouncementService announcementService,
                          CompanyRepository companyRepository,
                          JobRepository jobRepository,
                          UserRepository userRepository) {
        this.announcementService = announcementService;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("announcements", announcementService.getActiveAnnouncements());
        model.addAttribute("companies", companyRepository.findByApprovalStatusOrderByNameAsc(CompanyStatus.APPROVED));
        model.addAttribute("totalStudents", userRepository.countByRole(Role.ROLE_STUDENT));
        model.addAttribute("totalCompanies", companyRepository.countByApprovalStatus(CompanyStatus.APPROVED));
        model.addAttribute("totalActiveJobs", jobRepository.countByStatus(JobStatus.ACTIVE));
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
