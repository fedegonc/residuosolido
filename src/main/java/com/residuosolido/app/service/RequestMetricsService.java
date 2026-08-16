package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RequestMetricsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public RequestMetricsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Long> getUserDashboardStats(User user) {
        return MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user.$id").is(user.getId()), true);
    }

    public Map<String, Long> getOrgDashboardData(User organization) {
        return MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("organization.$id").is(organization.getId()), false);
    }

}
