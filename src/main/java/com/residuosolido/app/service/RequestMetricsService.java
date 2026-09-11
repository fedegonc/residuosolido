package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import org.bson.types.ObjectId;
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

    /**
     * Stats del dashboard del ciudadano.
     *
     * Request.user usa @DocumentReference(lazy=true), que guarda el ObjectId
     * directamente en el campo "user" (no como DBRef con $id). Por eso el match
     * es sobre "user" con un ObjectId, no sobre "user.$id" con un String.
     *
     * Si el ID no es un ObjectId válido (ej. en tests con mocks), usa el String directamente.
     */
    public Map<String, Long> getUserDashboardStats(User user) {
        return MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("user").is(toObjectIdOrString(user.getId())), true);
    }

    public Map<String, Long> getOrgDashboardData(User organization) {
        return MongoAggregationUtils.countByStatusFaceted(
                mongoTemplate, Criteria.where("organization").is(toObjectIdOrString(organization.getId())), false);
    }

    private static Object toObjectIdOrString(String id) {
        if (id != null && ObjectId.isValid(id)) {
            return new ObjectId(id);
        }
        return id;
    }

}
