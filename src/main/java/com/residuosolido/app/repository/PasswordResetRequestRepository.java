package com.residuosolido.app.repository;

import com.residuosolido.app.model.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    List<PasswordResetRequest> findByStatusOrderByRequestDateDesc(PasswordResetRequest.Status status);
    
    // Returns all requests ordered by requestDate descending
    List<PasswordResetRequest> findAllByOrderByRequestDateDesc();

    // Returns all requests with the given status (no specific ordering)
    List<PasswordResetRequest> findByStatus(PasswordResetRequest.Status status);
}
