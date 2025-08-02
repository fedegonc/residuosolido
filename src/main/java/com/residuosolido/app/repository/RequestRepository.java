package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUser(User user);
    List<Request> findByUserId(Long userId);
    List<Request> findByStatus(Request.RequestStatus status);
    
}
