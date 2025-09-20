package com.residuosolido.app.repository;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

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

    @Query("SELECT YEAR(u.createdAt) as year, MONTH(u.createdAt) as month, COUNT(u) as count FROM User u WHERE u.createdAt IS NOT NULL GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) ORDER BY year DESC, month DESC")
    List<Object[]> findUsersByMonth();

    @Query("SELECT u FROM User u WHERE lower(u.username) LIKE lower(concat('%', :q, '%')) OR lower(u.email) LIKE lower(concat('%', :q, '%')) OR lower(u.firstName) LIKE lower(concat('%', :q, '%')) OR lower(u.lastName) LIKE lower(concat('%', :q, '%'))")
    Page<User> search(@Param("q") String q, Pageable pageable);
}
