package com.portal.controller;

import com.portal.dto.PasswordResetConfirmDto;
import com.portal.dto.PasswordResetRequestDto;
import com.portal.dto.RecruiterRegisterDto;
import com.portal.dto.StudentRegisterDto;
import com.portal.exception.InvalidOperationException;
import com.portal.service.RecruiterService;
import com.portal.service.StudentService;
import com.portal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final StudentService studentService;
    private final RecruiterService recruiterService;
    private final UserService userService;

    public AuthController(StudentService studentService,
                          RecruiterService recruiterService,
                          UserService userService) {
        this.studentService = studentService;
        this.recruiterService = recruiterService;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email address or password. Please verify your credentials.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerChoice() {
        return "auth/register-choice";
    }

    @GetMapping("/register/student")
    public String showStudentRegisterForm(Model model) {
        if (!model.containsAttribute("studentDto")) {
            model.addAttribute("studentDto", new StudentRegisterDto());
        }
        return "auth/register-student";
    }

    @PostMapping("/register/student")
    public String processStudentRegistration(@Valid @ModelAttribute("studentDto") StudentRegisterDto dto,
                                             BindingResult bindingResult,
                                             RedirectAttributes redirectAttributes,
                                             Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register-student";
        }
        try {
            studentService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You may now sign in with your credentials.");
            return "redirect:/login";
        } catch (InvalidOperationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register-student";
        }
    }

    @GetMapping("/register/recruiter")
    public String showRecruiterRegisterForm(Model model) {
        if (!model.containsAttribute("recruiterDto")) {
            model.addAttribute("recruiterDto", new RecruiterRegisterDto());
        }
        return "auth/register-recruiter";
    }

    @PostMapping("/register/recruiter")
    public String processRecruiterRegistration(@Valid @ModelAttribute("recruiterDto") RecruiterRegisterDto dto,
                                               BindingResult bindingResult,
                                               RedirectAttributes redirectAttributes,
                                               Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register-recruiter";
        }
        try {
            recruiterService.registerRecruiter(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Recruiter account registered! Note: New companies require administrator approval before posting jobs.");
            return "redirect:/login";
        } catch (InvalidOperationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register-recruiter";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("resetRequestDto")) {
            model.addAttribute("resetRequestDto", new PasswordResetRequestDto());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@Valid @ModelAttribute("resetRequestDto") PasswordResetRequestDto dto,
                                        BindingResult bindingResult,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        try {
            String token = userService.createPasswordResetToken(dto.getEmail().trim().toLowerCase());
            model.addAttribute("successMessage", "Password reset token generated successfully. In development/testing, click the direct reset link below.");
            model.addAttribute("generatedToken", token);
            model.addAttribute("resetLink", "/reset-password?token=" + token);
            return "auth/forgot-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token,
                                        Model model) {
        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Missing password reset token.");
            return "auth/login";
        }
        try {
            userService.validatePasswordResetToken(token);
            PasswordResetConfirmDto dto = new PasswordResetConfirmDto();
            dto.setToken(token);
            model.addAttribute("confirmDto", dto);
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("confirmDto") PasswordResetConfirmDto dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            userService.resetPassword(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully! Please sign in with your new password.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }
}
