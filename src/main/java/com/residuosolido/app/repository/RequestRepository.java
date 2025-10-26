package com.residuosolido.app.repository;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"user", "organization"})
    Page<Request> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "organization"})
    @Query(value = """
            SELECT r FROM Request r
            LEFT JOIN r.user u
            LEFT JOIN r.organization o
            WHERE (:query IS NULL OR :query = '' OR
                  LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(o.username, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(CONCAT(COALESCE(o.firstName, ''), ' ', COALESCE(o.lastName, ''))) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(r.collectionAddress, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  EXISTS (
                      SELECT 1 FROM r.materials m
                      WHERE LOWER(COALESCE(m.name, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                  ))
            """,
            countQuery = """
            SELECT COUNT(r) FROM Request r
            LEFT JOIN r.user u
            LEFT JOIN r.organization o
            WHERE (:query IS NULL OR :query = '' OR
                  LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(o.username, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(CONCAT(COALESCE(o.firstName, ''), ' ', COALESCE(o.lastName, ''))) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(r.collectionAddress, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  EXISTS (
                      SELECT 1 FROM r.materials m
                      WHERE LOWER(COALESCE(m.name, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                  ))
            """
    )
    Page<Request> searchAll(@Param("query") String query, Pageable pageable);

    @Query("""
            SELECT r.status, COUNT(DISTINCT r)
            FROM Request r
            JOIN r.materials m
            WHERE m.id IN :materialIds
            GROUP BY r.status
            """)
    List<Object[]> countByStatusForMaterials(@Param("materialIds") List<Long> materialIds);

    @Query("""
            SELECT COUNT(DISTINCT r)
            FROM Request r
            JOIN r.materials m
            WHERE m.id IN :materialIds
            """)
    long countDistinctByMaterials(@Param("materialIds") List<Long> materialIds);

    @Query("""
            SELECT COUNT(DISTINCT r)
            FROM Request r
            JOIN r.materials m
            WHERE m.id IN :materialIds AND r.status = :status
            """)
    long countDistinctByMaterialsAndStatus(@Param("materialIds") List<Long> materialIds, @Param("status") RequestStatus status);
}
