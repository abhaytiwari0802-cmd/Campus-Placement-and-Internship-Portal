package com.portal.repository;

import com.portal.model.Resume;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByStudentOrderByUploadedAtDesc(User student);
    Optional<Resume> findFirstByStudentOrderByUploadedAtDesc(User student);
}
