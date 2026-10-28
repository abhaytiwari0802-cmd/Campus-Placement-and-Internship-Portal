package com.portal.repository;

import com.portal.model.Application;
import com.portal.model.ApplicationStatus;
import com.portal.model.Job;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByJobAndStudent(Job job, User student);
    boolean existsByJobAndStudent(Job job, User student);
    List<Application> findByStudentOrderByAppliedAtDesc(User student);
    List<Application> findByJobOrderByAppliedAtDesc(Job job);
    List<Application> findByJobInOrderByAppliedAtDesc(List<Job> jobs);
    long countByStudent(User student);
    long countByJob(Job job);
    long countByStatus(ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter")
    long countByRecruiter(@Param("recruiter") User recruiter);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter = :recruiter AND a.status = :status")
    long countByRecruiterAndStatus(@Param("recruiter") User recruiter, @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.job.recruiter = :recruiter ORDER BY a.appliedAt DESC")
    List<Application> findByRecruiter(@Param("recruiter") User recruiter);
}
