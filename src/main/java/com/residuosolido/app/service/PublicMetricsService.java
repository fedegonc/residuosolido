package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
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
public class PublicMetricsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public PublicMetricsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
        return MongoAggregationUtils.extractSimpleCount(mappedResults.get(0), "total");
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
            metrics.put(city, MongoAggregationUtils.extractSimpleCount(result, "count"));
        }
        return metrics;
    }
}
