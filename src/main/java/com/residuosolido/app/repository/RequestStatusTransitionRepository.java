package com.residuosolido.app.repository;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.RequestStatusTransition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestStatusTransitionRepository extends MongoRepository<RequestStatusTransition, String> {

    List<RequestStatusTransition> findByRequestIdOrderByTimestampAsc(String requestId);

    List<RequestStatusTransition> findByOrganizationAndToStatusAndTimestampBetweenOrderByTimestampDesc(
            Organization organization,
            RequestStatus toStatus,
            LocalDateTime startTime,
            LocalDateTime endTime);

    @Query("{ 'organization._id': ?0, 'toStatus': ?1, 'timestamp': { $gte: ?2, $lt: ?3 } }")
    List<RequestStatusTransition> countTransitionsForMonth(
            String organizationId,
            RequestStatus toStatus,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth);
}
