package com.residuosolido.app.repository;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    List<Feedback> findByUser(User user);
}
