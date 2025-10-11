package com.residuosolido.app.repository;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    List<Feedback> findByUserOrderByCreatedAtDesc(User user);
    
    // Método alternativo con query explícita
    @Query("SELECT f FROM Feedback f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Feedback> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Método legacy para compatibilidad
    default List<Feedback> findByUser(User user) {
        return findByUserOrderByCreatedAtDesc(user);
    }

    long countByUserId(Long userId);
}
