package com.residuosolido.app.repository;

import com.residuosolido.app.model.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationEventRepository extends MongoRepository<NotificationEvent, String> {

    List<NotificationEvent> findByProcessedFalseOrderByCreatedAtAsc();

    List<NotificationEvent> findByUserIdAndProcessedTrue(String userId);
}
