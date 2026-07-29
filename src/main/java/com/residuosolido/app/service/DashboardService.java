package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DashboardService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DashboardService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Long> getOrgDashboardData(User organization) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("organization.$id").is(organization.getId())),
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
                ).as("completed")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "requests", Map.class);
        Map<String, Long> data = new java.util.HashMap<>();
        data.put("pending", 0L);
        data.put("inProgress", 0L);
        data.put("completed", 0L);
        if (!results.getMappedResults().isEmpty()) {
            Map result = results.getMappedResults().get(0);
            data.put("pending", extractCount(result, "pending"));
            data.put("inProgress", extractCount(result, "inProgress"));
            data.put("completed", extractCount(result, "completed"));
        }
        return data;
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
}
