package com.portal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model, HttpServletRequest request) {
        logger.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public String handleUnauthorizedAccess(UnauthorizedAccessException ex, Model model, HttpServletRequest request) {
        logger.warn("Unauthorized access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status", 403);
        model.addAttribute("error", "Access Denied");
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(InvalidOperationException.class)
    public String handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.warn("Invalid operation at {}: {}", request.getRequestURI(), ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return (referer != null && !referer.isEmpty()) ? "redirect:" + referer : "redirect:/";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.warn("File upload limit exceeded at {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", "Uploaded file exceeds maximum limit of 5MB!");
        return "redirect:/student/profile";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        logger.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "An unexpected error occurred. Please contact the administrator if this persists.");
        return "error/500";
    }
}
