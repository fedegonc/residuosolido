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

class DashboardServiceTest {

    private MongoTemplate mongoTemplate;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        service = new DashboardService(mongoTemplate);
    }

    private User organization(String id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.ORGANIZATION);
        return u;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockAggregationResult(Map<String, Object> mappedResult) {
        AggregationResults<Map> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(mappedResult == null ? List.of() : List.of(mappedResult));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("requests"), eq(Map.class)))
                .thenReturn(results);
    }

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
    void getOrgDashboardData_returnsAllThreeKeys() {
        mockAggregationResult(null);
        Map<String, Long> data = service.getOrgDashboardData(organization("org1"));
        assertEquals(3, data.size());
        assertTrue(data.containsKey("pending"));
        assertTrue(data.containsKey("inProgress"));
        assertTrue(data.containsKey("completed"));
    }
}
