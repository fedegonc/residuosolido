package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUserId(Long userId);
    List<Request> findByOrganizationId(Long organizationId);
    List<Request> findByStatus(Request.RequestStatus status);
}
