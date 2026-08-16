package com.residuosolido.app.service;

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

class PublicMetricsServiceTest {

    private MongoTemplate mongoTemplate;
    private PublicMetricsService service;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        service = new PublicMetricsService(mongoTemplate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockAggregationResult(List<Map> mappedResults) {
        AggregationResults<Map> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(mappedResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("requests"), eq(Map.class)))
                .thenReturn(results);
    }

    // ─── getPublicTotalCompleted ───

    @Test
    void getPublicTotalCompleted_noResults_returnsZero() {
        mockAggregationResult(List.of());
        assertEquals(0L, service.getPublicTotalCompleted());
    }

    @Test
    void getPublicTotalCompleted_withCount_returnsCount() {
        mockAggregationResult(List.of(Map.of("total", 42)));
        assertEquals(42L, service.getPublicTotalCompleted());
    }

    @Test
    void getPublicTotalCompleted_nullCount_returnsZero() {
        java.util.HashMap<String, Object> map = new java.util.HashMap<>();
        map.put("total", null);
        mockAggregationResult(List.of(map));
        assertEquals(0L, service.getPublicTotalCompleted());
    }

    // ─── getPublicMetricsByCity ───

    @Test
    void getPublicMetricsByCity_noResults_returnsEmptyMap() {
        mockAggregationResult(List.of());
        Map<String, Long> metrics = service.getPublicMetricsByCity();
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());
    }

    @Test
    void getPublicMetricsByCity_withMultipleCities_returnsSortedByCountDesc() {
        mockAggregationResult(List.of(
                Map.of("_id", "RIVERA", "count", 15),
                Map.of("_id", "LIVRAMENTO", "count", 8)
        ));
        Map<String, Long> metrics = service.getPublicMetricsByCity();
        assertEquals(2, metrics.size());
        assertEquals(15L, metrics.get("RIVERA"));
        assertEquals(8L, metrics.get("LIVRAMENTO"));
    }

    @Test
    void getPublicMetricsByCity_singleCity_returnsOneEntry() {
        mockAggregationResult(List.of(
                Map.of("_id", "RIVERA", "count", 3)
        ));
        Map<String, Long> metrics = service.getPublicMetricsByCity();
        assertEquals(1, metrics.size());
        assertEquals(3L, metrics.get("RIVERA"));
    }
}
