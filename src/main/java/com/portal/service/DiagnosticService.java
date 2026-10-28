package com.portal.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DiagnosticService {

    private final JdbcTemplate jdbcTemplate;

    public DiagnosticService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();

        // JVM and runtime
        health.put("Java Version", System.getProperty("java.version"));
        health.put("JVM Name", System.getProperty("java.vm.name"));
        health.put("OS", System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        health.put("Uptime (minutes)", ManagementFactory.getRuntimeMXBean().getUptime() / 60000);
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        health.put("Memory", (totalMem - freeMem) + "MB used / " + totalMem + "MB total");

        // Database connection test
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            health.put("Database Status", "ONLINE");
            health.put("MySQL Version", dbVersion);

            // Table row counts
            Map<String, Long> tableCounts = new LinkedHashMap<>();
            tableCounts.put("users", getCountSafe("users"));
            tableCounts.put("student_profiles", getCountSafe("student_profiles"));
            tableCounts.put("recruiter_profiles", getCountSafe("recruiter_profiles"));
            tableCounts.put("companies", getCountSafe("companies"));
            tableCounts.put("jobs", getCountSafe("jobs"));
            tableCounts.put("applications", getCountSafe("applications"));
            tableCounts.put("interviews", getCountSafe("interviews"));
            tableCounts.put("resumes", getCountSafe("resumes"));
            tableCounts.put("notifications", getCountSafe("notifications"));
            tableCounts.put("announcements", getCountSafe("announcements"));
            tableCounts.put("audit_logs", getCountSafe("audit_logs"));
            health.put("Table Counts", tableCounts);
        } catch (Exception e) {
            health.put("Database Status", "ERROR: " + e.getMessage());
        }

        return health;
    }

    private Long getCountSafe(String table) {
        try {
            return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        } catch (Exception e) {
            return -1L;
        }
    }
}
