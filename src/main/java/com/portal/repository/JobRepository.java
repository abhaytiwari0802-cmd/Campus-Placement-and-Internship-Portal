package com.portal.repository;

import com.portal.model.Company;
import com.portal.model.Job;
import com.portal.model.JobStatus;
import com.portal.model.JobType;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByRecruiter(User recruiter);
    List<Job> findByRecruiterOrderByCreatedAtDesc(User recruiter);
    List<Job> findByCompany(Company company);
    long countByStatus(JobStatus status);
    long countByRecruiter(User recruiter);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.deadline >= :today ORDER BY j.createdAt DESC")
    List<Job> findActiveJobs(@Param("status") JobStatus status, @Param("today") LocalDate today);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.deadline >= :today " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.company.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "ORDER BY j.createdAt DESC")
    List<Job> searchJobs(@Param("keyword") String keyword,
                         @Param("jobType") JobType jobType,
                         @Param("location") String location,
                         @Param("today") LocalDate today);
}
