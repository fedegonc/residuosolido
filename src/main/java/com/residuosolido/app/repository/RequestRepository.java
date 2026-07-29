package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<Request, String> {
    List<Request> findByUser(User user, Pageable pageable);
    List<Request> findByOrganizationOrderByCreatedAtDesc(User organization, Pageable pageable);
    List<Request> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);
    List<Request> findByOrganizationAndStatusOrderByCreatedAtDesc(User organization, RequestStatus status, Pageable pageable);
}
