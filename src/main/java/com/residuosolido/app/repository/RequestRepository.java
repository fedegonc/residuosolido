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
    List<Request> findByUser(User user);
    List<Request> findByOrganizationOrderByCreatedAtDesc(User organization);
    List<Request> findByStatusInAndOrganizationOrderByCreatedAtDesc(List<RequestStatus> statuses, User organization);
    List<Request> findByStatus(RequestStatus status);
    List<Request> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);
    long countByUser(User user);
    long countByUserAndStatus(User user, RequestStatus status);
    long countByOrganizationAndStatus(User organization, RequestStatus status);
    List<Request> findByOrganizationAndStatusOrderByCreatedAtDesc(User organization, RequestStatus status, Pageable pageable);
}
