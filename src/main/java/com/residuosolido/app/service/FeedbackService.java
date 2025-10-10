package com.residuosolido.app.service;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        List<Feedback> feedbacks = feedbackRepository.findAllByOrderByCreatedAtDesc();
        // Forzar inicialización de propiedades lazy del User
        feedbacks.forEach(feedback -> {
            if (feedback.getUser() != null) {
                feedback.getUser().getUsername();
                feedback.getUser().getFirstName();
                feedback.getUser().getLastName();
                feedback.getUser().getEmail();
            }
        });
        return feedbacks;
    }

    @Transactional(readOnly = true)
    public List<Feedback> findAllOrderedByCreatedAtDesc() {
        return findAll();
    }
    
    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }
    
    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
    
    public void deleteById(Long id) {
        feedbackRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Feedback> findByUser(User user) {
        List<Feedback> feedbacks = feedbackRepository.findByUser(user);
        // Forzar inicialización de propiedades lazy del User
        feedbacks.forEach(feedback -> {
            if (feedback.getUser() != null) {
                feedback.getUser().getUsername();
                feedback.getUser().getFirstName();
                feedback.getUser().getLastName();
                feedback.getUser().getEmail();
            }
        });
        return feedbacks;
    }
    
    public long countByUserId(Long userId) {
        return feedbackRepository.countByUserId(userId);
    }

    public long count() {
        return feedbackRepository.count();
    }

    /**
     * Marca un feedback como leído
     */
    public void markAsRead(Long id) {
        Optional<Feedback> feedback = findById(id);
        if (feedback.isPresent()) {
            Feedback fb = feedback.get();
            fb.setIsRead(true);
            save(fb);
        }
    }

    /**
     * Responde a un feedback
     */
    public void respondToFeedback(Long id, String response) {
        Optional<Feedback> feedback = findById(id);
        if (feedback.isPresent()) {
            Feedback fb = feedback.get();
            fb.setAdminResponse(response);
            fb.setRespondedAt(java.time.LocalDateTime.now());
            save(fb);
        }
    }

    /**
     * Cuenta feedbacks pendientes (sin leer)
     */
    
}
