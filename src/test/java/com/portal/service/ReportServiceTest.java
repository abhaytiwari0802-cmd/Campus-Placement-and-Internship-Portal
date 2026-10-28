package com.portal.service;

import com.portal.dto.PlacementReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(jdbcTemplate);
    }

    @Test
    @DisplayName("Should query company placement reports via JdbcTemplate")
    void testGetCompanyPlacementReport() {
        PlacementReportDto mockRow = new PlacementReportDto("Google India", "SDE-1", "FULL_TIME", 10L, 5L, 3L, 2L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(mockRow));

        List<PlacementReportDto> reports = reportService.getCompanyPlacementReport();

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("Google India", reports.get(0).getCompanyName());
        assertEquals(2L, reports.get(0).getSelectedCount());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class));
    }

    @Test
    @DisplayName("Should query branch placement statistics via JdbcTemplate")
    void testGetBranchPlacementStatistics() {
        Map<String, Object> mockBranch = Map.of(
                "branch", "Computer Science",
                "registered_students", 25L,
                "total_applications", 50L,
                "placed_students", 20L
        );
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(mockBranch));

        List<Map<String, Object>> stats = reportService.getBranchPlacementStatistics();

        assertNotNull(stats);
        assertEquals(1, stats.size());
        assertEquals("Computer Science", stats.get(0).get("branch"));
        verify(jdbcTemplate).queryForList(anyString());
    }

    @Test
    @DisplayName("Should query application status distribution via JdbcTemplate")
    void testGetApplicationStatusDistribution() {
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            // Verify query is called
            return null;
        }).when(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));

        Map<String, Long> distribution = reportService.getApplicationStatusDistribution();
        assertNotNull(distribution);
        verify(jdbcTemplate).query(anyString(), any(RowCallbackHandler.class));
    }
}
