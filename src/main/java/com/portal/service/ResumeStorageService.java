package com.portal.service;

import com.portal.exception.InvalidOperationException;
import com.portal.exception.ResourceNotFoundException;
import com.portal.exception.UnauthorizedAccessException;
import com.portal.model.Application;
import com.portal.model.Resume;
import com.portal.model.Role;
import com.portal.model.User;
import com.portal.repository.ApplicationRepository;
import com.portal.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResumeStorageService {

    private final Path fileStorageLocation;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".pdf", ".doc", ".docx");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public ResumeStorageService(@Value("${portal.upload.dir:./uploads/resumes}") String uploadDir,
                                ResumeRepository resumeRepository,
                                ApplicationRepository applicationRepository) {
        this.resumeRepository = resumeRepository;
        this.applicationRepository = applicationRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new InvalidOperationException("Could not create resume upload directory: " + ex.getMessage());
        }
    }

    @Transactional
    public Resume storeFile(MultipartFile file, User student) {
        if (file.isEmpty()) {
            throw new InvalidOperationException("Failed to store empty file.");
        }

        String rawOriginalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume.pdf");

        // Prevent path traversal attack
        if (rawOriginalFilename.contains("..")) {
            throw new InvalidOperationException("Filename contains invalid path sequence: " + rawOriginalFilename);
        }

        // Validate extension
        String extension = "";
        int extIndex = rawOriginalFilename.lastIndexOf(".");
        if (extIndex >= 0) {
            extension = rawOriginalFilename.substring(extIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidOperationException("Only PDF, DOC, and DOCX files are permitted for resumes.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidOperationException("Invalid file type: " + contentType);
        }

        // Generate safe stored filename
        String storedFilename = "resume_" + student.getId() + "_" + UUID.randomUUID() + extension;
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InvalidOperationException("Could not store file: " + rawOriginalFilename + ". Error: " + ex.getMessage());
        }

        Resume resume = new Resume(
                student,
                rawOriginalFilename,
                storedFilename,
                contentType != null ? contentType : "application/octet-stream",
                file.getSize(),
                targetLocation.toString()
        );

        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public Optional<Resume> getLatestResumeForStudent(User student) {
        return resumeRepository.findFirstByStudentOrderByUploadedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public Resume getResumeById(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long resumeId, User currentUser) {
        Resume resume = getResumeById(resumeId);

        // Security Authorization Check:
        // 1. If admin -> allowed
        // 2. If student -> must be their own resume
        // 3. If recruiter -> must have an application from this student for a job belonging to this recruiter
        boolean authorized = false;

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            authorized = true;
        } else if (currentUser.getRole() == Role.ROLE_STUDENT && resume.getStudent().getId().equals(currentUser.getId())) {
            authorized = true;
        } else if (currentUser.getRole() == Role.ROLE_RECRUITER) {
            List<Application> recruiterApplications = applicationRepository.findByRecruiter(currentUser);
            authorized = recruiterApplications.stream()
                    .anyMatch(a -> a.getStudent().getId().equals(resume.getStudent().getId()));
        }

        if (!authorized) {
            throw new UnauthorizedAccessException("You are not authorized to view or download this resume.");
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(resume.getStoredFilename()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file not found on disk: " + resume.getOriginalFilename());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path invalid for resume: " + resume.getOriginalFilename());
        }
    }
}
