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

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUser(User user);
    List<Request> findByUserId(Long userId);
    List<Request> findByStatus(RequestStatus status);
    List<Request> findByOrganization(User organization);
    List<Request> findTop5ByUserOrderByCreatedAtDesc(User user);

    long countByStatus(RequestStatus status);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT r.user.id) FROM Request r WHERE r.createdAt IS NOT NULL AND r.createdAt > :since")
    long countDistinctUsersWithRequestsAfter(@Param("since") LocalDateTime since);

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
    
    @Query("""
            SELECT COUNT(DISTINCT r)
            FROM Request r
            WHERE r.status = :status AND
            ((:includeMaterialless = true AND r.materials IS EMPTY) OR
             EXISTS (SELECT 1 FROM r.materials m WHERE m.id IN :materialIds))
            """)
    long countByStatusWithOptionalMaterials(
        @Param("status") RequestStatus status,
        @Param("materialIds") List<Long> materialIds,
        @Param("includeMaterialless") boolean includeMaterialless);


    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("SELECT r FROM Request r WHERE r.id = :id")
    java.util.Optional<Request> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("SELECT r FROM Request r WHERE r.organization = :organization ORDER BY r.createdAt DESC")
    List<Request> findByOrganizationWithDetails(@Param("organization") User organization);

    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("SELECT r FROM Request r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Request> findByStatusWithDetails(@Param("status") RequestStatus status);

    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("""
            SELECT DISTINCT r FROM Request r
            WHERE r.status = :status AND r.organization = :organization
            ORDER BY r.createdAt DESC
            """)
    List<Request> findByStatusAndOrganizationWithDetails(@Param("status") RequestStatus status,
                                                          @Param("organization") User organization);

    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("""
            SELECT DISTINCT r FROM Request r
            WHERE r.status IN :statuses AND r.organization = :organization
            ORDER BY r.createdAt DESC
            """)
    List<Request> findByStatusesAndOrganizationWithDetails(@Param("statuses") List<RequestStatus> statuses,
                                                            @Param("organization") User organization);

    @Query("""
            SELECT COUNT(DISTINCT r)
            FROM Request r
            WHERE r.status IN :statuses AND r.organization = :organization
            """)
    long countByStatusesAndOrganization(@Param("statuses") List<RequestStatus> statuses,
                                        @Param("organization") User organization);

    // Paso 1: Traer solo IDs de las top N solicitudes (LIMIT en BD)
    // Incluye solicitudes sin materiales y solicitudes con materiales específicos
    @Query(value = """
            SELECT DISTINCT r.id, r.created_at FROM requests r
            LEFT JOIN request_materials m ON r.id = m.request_id
            WHERE r.status = :status AND
            (m.material_id IN :materialIds OR
             NOT EXISTS (SELECT 1 FROM request_materials rm WHERE rm.request_id = r.id))
            ORDER BY r.created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopIdsByStatusAndMaterials(@Param("status") String status,
                                                   @Param("materialIds") List<Long> materialIds,
                                                   @Param("limit") int limit);
    
    // Paso 2: Traer solicitudes completas por IDs (con eager loading)
    @EntityGraph(attributePaths = {"user", "organization", "materials"})
    @Query("SELECT r FROM Request r WHERE r.id IN :ids ORDER BY r.createdAt DESC")
    List<Request> findByIdsWithDetails(@Param("ids") List<Long> ids);
}
