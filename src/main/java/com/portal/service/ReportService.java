package com.portal.service;

import com.portal.dto.PlacementReportDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Aggregated Placement Report grouped by Company and Job Posting.
     * Implemented using JdbcTemplate for optimal relational aggregation performance.
     */
    @Transactional(readOnly = true)
    public List<PlacementReportDto> getCompanyPlacementReport() {
        String sql = """
            SELECT 
                c.name AS company_name,
                j.title AS job_title,
                j.job_type AS job_type,
                COUNT(a.id) AS total_applications,
                SUM(CASE WHEN a.status IN ('SHORTLISTED', 'INTERVIEW_SCHEDULED', 'SELECTED') THEN 1 ELSE 0 END) AS shortlisted_count,
                SUM(CASE WHEN a.status IN ('INTERVIEW_SCHEDULED', 'SELECTED') THEN 1 ELSE 0 END) AS interviews_count,
                SUM(CASE WHEN a.status = 'SELECTED' THEN 1 ELSE 0 END) AS selected_count
            FROM companies c
            INNER JOIN jobs j ON c.id = j.company_id
            LEFT JOIN applications a ON j.id = a.job_id
            GROUP BY c.id, c.name, j.id, j.title, j.job_type
            ORDER BY c.name ASC, j.title ASC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlacementReportDto(
                rs.getString("company_name"),
                rs.getString("job_title"),
                rs.getString("job_type"),
                rs.getLong("total_applications"),
                rs.getLong("shortlisted_count"),
                rs.getLong("interviews_count"),
                rs.getLong("selected_count")
        ));
    }

    /**
     * Placement Analytics grouped by Academic Branch.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBranchPlacementStatistics() {
        String sql = """
            SELECT 
                sp.branch AS branch,
                COUNT(DISTINCT sp.id) AS registered_students,
                COUNT(DISTINCT a.id) AS total_applications,
                COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN sp.id END) AS placed_students
            FROM student_profiles sp
            LEFT JOIN applications a ON sp.user_id = a.student_id
            GROUP BY sp.branch
            ORDER BY sp.branch ASC
        """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Application status breakdown across the entire system.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getApplicationStatusDistribution() {
        String sql = """
            SELECT status, COUNT(*) AS count
            FROM applications
            GROUP BY status
        """;

        Map<String, Long> distribution = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            distribution.put(rs.getString("status"), rs.getLong("count"));
        });
        return distribution;
    }
}
