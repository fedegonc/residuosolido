package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RequestMetricsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public RequestMetricsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Long> getUserDashboardStats(User user) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("user.$id").is(user.getId())),
                Aggregation.facet(
                        Aggregation.match(Criteria.where("status").is(RequestStatus.PENDING)),
                        Aggregation.count().as("count")
                ).as("pending"),
                Aggregation.facet(
                        Aggregation.match(Criteria.where("status").is(RequestStatus.IN_PROGRESS)),
                        Aggregation.count().as("count")
                ).as("inProgress"),
                Aggregation.facet(
                        Aggregation.match(Criteria.where("status").is(RequestStatus.COMPLETED)),
                        Aggregation.count().as("count")
                ).as("completed"),
                Aggregation.facet(
                        Aggregation.count().as("count")
                ).as("total")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "requests", Map.class);
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", 0L);
        stats.put("pending", 0L);
        stats.put("inProgress", 0L);
        stats.put("completed", 0L);
        if (!results.getMappedResults().isEmpty()) {
            Map result = results.getMappedResults().get(0);
            stats.put("total", extractCount(result, "total"));
            stats.put("pending", extractCount(result, "pending"));
            stats.put("inProgress", extractCount(result, "inProgress"));
            stats.put("completed", extractCount(result, "completed"));
        }
        return stats;
    }

    @SuppressWarnings("unchecked")
    private long extractCount(Map result, String facetName) {
        Object facet = result.get(facetName);
        if (facet instanceof java.util.List<?> list && !list.isEmpty()) {
            Object count = ((Map<String, Object>) list.get(0)).get("count");
            return count instanceof Number ? ((Number) count).longValue() : 0L;
        }
        return 0L;
    }

    public long getPublicTotalCompleted() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(RequestStatus.COMPLETED)),
                Aggregation.count().as("total")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "requests", Map.class);
        List<Map> mappedResults = results.getMappedResults();
        if (mappedResults.isEmpty()) {
            return 0;
        }
        Object total = mappedResults.get(0).get("total");
        return total instanceof Number ? ((Number) total).longValue() : 0;
    }

    public Map<String, Long> getPublicMetricsByCity() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is(RequestStatus.COMPLETED)
                        .and("city").ne(null)),
                Aggregation.group("city").count().as("count"),
                Aggregation.sort(org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Order.desc("count"),
                        org.springframework.data.domain.Sort.Order.asc("_id")))
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "requests", Map.class);
        Map<String, Long> metrics = new LinkedHashMap<>();
        for (Map result : results.getMappedResults()) {
            String city = result.get("_id").toString();
            Long count = ((Number) result.get("count")).longValue();
            metrics.put(city, count);
        }
        return metrics;
    }
}
