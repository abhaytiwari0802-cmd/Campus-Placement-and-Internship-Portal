package com.portal.repository;

import com.portal.model.Application;
import com.portal.model.Interview;
import com.portal.model.InterviewStatus;
import com.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplication(Application application);
    List<Interview> findByApplicationOrderByInterviewDateDesc(Application application);
    long countByStatus(InterviewStatus status);

    @Query("SELECT i FROM Interview i WHERE i.application.student = :student ORDER BY i.interviewDate ASC, i.interviewTime ASC")
    List<Interview> findByStudent(@Param("student") User student);

    @Query("SELECT i FROM Interview i WHERE i.application.student = :student AND i.interviewDate >= :today AND i.status = 'SCHEDULED' ORDER BY i.interviewDate ASC, i.interviewTime ASC")
    List<Interview> findUpcomingByStudent(@Param("student") User student, @Param("today") LocalDate today);

    @Query("SELECT i FROM Interview i WHERE i.application.job.recruiter = :recruiter ORDER BY i.interviewDate DESC, i.interviewTime DESC")
    List<Interview> findByRecruiter(@Param("recruiter") User recruiter);

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.application.job.recruiter = :recruiter")
    long countByRecruiter(@Param("recruiter") User recruiter);
}
