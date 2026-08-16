package com.residuosolido.app.service;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestMetricsServiceTest {

    private MongoTemplate mongoTemplate;
    private RequestMetricsService service;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        service = new RequestMetricsService(mongoTemplate);
    }

    private User organization(String id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.ORGANIZATION);
        return u;
    }

    private User user(String id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.USER);
        return u;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockAggregationResult(Map<String, Object> mappedResult) {
        AggregationResults<Map> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(mappedResult == null ? List.of() : List.of(mappedResult));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("requests"), eq(Map.class)))
                .thenReturn(results);
    }

    // ─── getOrgDashboardData (ex-DashboardService) ───

    @Test
    void getOrgDashboardData_noResults_returnsZeroedCounts() {
        mockAggregationResult(null);
        Map<String, Long> data = service.getOrgDashboardData(organization("org1"));
        assertEquals(0L, data.get("pending"));
        assertEquals(0L, data.get("inProgress"));
        assertEquals(0L, data.get("completed"));
    }

    @Test
    void getOrgDashboardData_withResults_extractsCounts() {
        Map<String, Object> mapped = Map.of(
                "pending", List.of(Map.of("count", 3)),
                "inProgress", List.of(Map.of("count", 2)),
                "completed", List.of(Map.of("count", 7))
        );
        mockAggregationResult(mapped);
        Map<String, Long> data = service.getOrgDashboardData(organization("org1"));
        assertEquals(3L, data.get("pending"));
        assertEquals(2L, data.get("inProgress"));
        assertEquals(7L, data.get("completed"));
    }

    @Test
    void getOrgDashboardData_returnsOnlyThreeKeys_noTotal() {
        mockAggregationResult(null);
        Map<String, Long> data = service.getOrgDashboardData(organization("org1"));
        assertEquals(3, data.size());
        assertTrue(data.containsKey("pending"));
        assertTrue(data.containsKey("inProgress"));
        assertTrue(data.containsKey("completed"));
        assertFalse(data.containsKey("total"));
    }

    // ─── getUserDashboardStats ───

    @Test
    void getUserDashboardStats_noResults_returnsZeroedCounts() {
        mockAggregationResult(null);
        Map<String, Long> data = service.getUserDashboardStats(user("u1"));
        assertEquals(0L, data.get("pending"));
        assertEquals(0L, data.get("inProgress"));
        assertEquals(0L, data.get("completed"));
        assertEquals(0L, data.get("total"));
    }

    @Test
    void getUserDashboardStats_withResults_extractsCounts() {
        Map<String, Object> mapped = Map.of(
                "pending", List.of(Map.of("count", 1)),
                "inProgress", List.of(Map.of("count", 2)),
                "completed", List.of(Map.of("count", 4)),
                "total", List.of(Map.of("count", 7))
        );
        mockAggregationResult(mapped);
        Map<String, Long> data = service.getUserDashboardStats(user("u1"));
        assertEquals(1L, data.get("pending"));
        assertEquals(2L, data.get("inProgress"));
        assertEquals(4L, data.get("completed"));
        assertEquals(7L, data.get("total"));
    }

    @Test
    void getUserDashboardStats_returnsFourKeys_includingTotal() {
        mockAggregationResult(null);
        Map<String, Long> data = service.getUserDashboardStats(user("u1"));
        assertEquals(4, data.size());
        assertTrue(data.containsKey("total"));
    }
}
