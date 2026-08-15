package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MongoAggregationUtils {

    private MongoAggregationUtils() {}

    @SuppressWarnings("unchecked")
    public static long extractCount(Map result, String facetName) {
        Object facet = result.get(facetName);
        if (facet instanceof List<?> list && !list.isEmpty()) {
            Object count = ((Map<String, Object>) list.get(0)).get("count");
            return count instanceof Number ? ((Number) count).longValue() : 0L;
        }
        return 0L;
    }

    public static long extractSimpleCount(Map result, String fieldName) {
        Object value = result.get(fieldName);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * Cuenta solicitudes por estado (PENDING/IN_PROGRESS/COMPLETED) filtradas por un
     * criterio base (ej. organization.$id o user.$id), usando un facet aggregation.
     * Compartido por DashboardService (organización) y RequestMetricsService (usuario)
     * para evitar duplicar la construcción del pipeline.
     */
    @SuppressWarnings("rawtypes")
    public static Map<String, Long> countByStatusFaceted(MongoTemplate mongoTemplate, Criteria baseMatch, boolean includeTotal) {
        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(Aggregation.match(baseMatch));
        ops.add(Aggregation.facet(
                Aggregation.match(Criteria.where("status").is(RequestStatus.PENDING)),
                Aggregation.count().as("count")
        ).as("pending"));
        ops.add(Aggregation.facet(
                Aggregation.match(Criteria.where("status").is(RequestStatus.IN_PROGRESS)),
                Aggregation.count().as("count")
        ).as("inProgress"));
        ops.add(Aggregation.facet(
                Aggregation.match(Criteria.where("status").is(RequestStatus.COMPLETED)),
                Aggregation.count().as("count")
        ).as("completed"));
        if (includeTotal) {
            ops.add(Aggregation.facet(Aggregation.count().as("count")).as("total"));
        }

        AggregationResults<Map> results = mongoTemplate.aggregate(Aggregation.newAggregation(ops), "requests", Map.class);
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", 0L);
        stats.put("inProgress", 0L);
        stats.put("completed", 0L);
        if (includeTotal) {
            stats.put("total", 0L);
        }
        if (!results.getMappedResults().isEmpty()) {
            Map result = results.getMappedResults().get(0);
            stats.put("pending", extractCount(result, "pending"));
            stats.put("inProgress", extractCount(result, "inProgress"));
            stats.put("completed", extractCount(result, "completed"));
            if (includeTotal) {
                stats.put("total", extractCount(result, "total"));
            }
        }
        return stats;
    }
}
