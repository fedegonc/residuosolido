package com.residuosolido.app.repository;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleAndActive(Role role, boolean active);
    long countByRole(Role role);
    long countByRoleAndActive(Role role, boolean active);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT u FROM User u WHERE lower(u.username) LIKE lower(concat('%', :q, '%')) OR lower(u.email) LIKE lower(concat('%', :q, '%')) OR lower(u.firstName) LIKE lower(concat('%', :q, '%')) OR lower(u.lastName) LIKE lower(concat('%', :q, '%'))")
    Page<User> search(@Param("q") String q, Pageable pageable);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET profile_completed = true WHERE id = :userId", nativeQuery = true)
    int markProfileAsCompleted(@Param("userId") Long userId);
    
    // JOIN FETCH para cargar materials junto con el usuario (evita LazyInitializationException)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.materials WHERE u.id = :id")
    @QueryHints(@QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false"))
    Optional<User> findByIdWithMaterials(@Param("id") Long id);
    
    // JOIN FETCH para cargar materials junto con el usuario por username
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.materials WHERE u.username = :username")
    @QueryHints(@QueryHint(name = org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, value = "false"))
    Optional<User> findByUsernameWithMaterials(@Param("username") String username);
}
