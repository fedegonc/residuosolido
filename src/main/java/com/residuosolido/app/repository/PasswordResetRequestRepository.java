package com.residuosolido.app.repository;

import com.residuosolido.app.model.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    List<PasswordResetRequest> findByStatusOrderByRequestDateDesc(PasswordResetRequest.Status status);
}
