package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends MongoRepository<Request, String> {
    List<Request> findByUser(User user);
    List<Request> findByOrganizationOrderByCreatedAtDesc(User organization);
    List<Request> findByStatusInAndOrganizationOrderByCreatedAtDesc(List<RequestStatus> statuses, User organization);
    long countByStatusInAndOrganization(List<RequestStatus> statuses, User organization);
    List<Request> findByMaterialsIdIn(List<String> materialIds);
    long countByMaterialsIdIn(List<String> materialIds);
}
