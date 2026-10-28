package com.portal.repository;

import com.portal.model.Job;
import com.portal.model.SavedJob;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    Optional<SavedJob> findByStudentAndJob(User student, Job job);
    boolean existsByStudentAndJob(User student, Job job);
    List<SavedJob> findByStudentOrderBySavedAtDesc(User student);
    long countByStudent(User student);
    void deleteByStudentAndJob(User student, Job job);
}
