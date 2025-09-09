package com.residuosolido.app.service;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }
    
    public List<Feedback> findAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Feedback> findAllOrderedByCreatedAtDesc() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
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
    
    public List<Feedback> findByUser(User user) {
        return feedbackRepository.findByUser(user);
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
