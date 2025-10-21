package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUser(User user);
    List<Request> findByUserId(Long userId);
    List<Request> findByStatus(RequestStatus status);
    List<Request> findByOrganization(User organization);
    List<Request> findTop5ByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT YEAR(r.createdAt), MONTH(r.createdAt), COUNT(r) FROM Request r GROUP BY YEAR(r.createdAt), MONTH(r.createdAt) ORDER BY YEAR(r.createdAt), MONTH(r.createdAt)")
    List<Object[]> countRequestsByMonth();

    @Query("SELECT r.user.id, r.status, COUNT(r) FROM Request r WHERE r.user.id IN :userIds GROUP BY r.user.id, r.status")
    List<Object[]> countByUserIdsAndStatus(@Param("userIds") List<Long> userIds);
}
