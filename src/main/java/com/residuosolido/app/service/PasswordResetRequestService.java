package com.residuosolido.app.service;

import com.residuosolido.app.model.PasswordResetRequest;
import com.residuosolido.app.repository.PasswordResetRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetRequestService {

    @Autowired
    private PasswordResetRequestRepository passwordResetRequestRepository;

    public List<PasswordResetRequest> findAll() {
        return passwordResetRequestRepository.findAll();
    }

    public List<PasswordResetRequest> findAllOrderedByRequestDateDesc() {
        return passwordResetRequestRepository.findAllByOrderByRequestDateDesc();
    }

    public Optional<PasswordResetRequest> findById(Long id) {
        return passwordResetRequestRepository.findById(id);
    }

    public PasswordResetRequest save(PasswordResetRequest request) {
        return passwordResetRequestRepository.save(request);
    }

    public void deleteById(Long id) {
        passwordResetRequestRepository.deleteById(id);
    }

    public List<PasswordResetRequest> findByStatus(PasswordResetRequest.Status status) {
        return passwordResetRequestRepository.findByStatus(status);
    }

    // Métodos consolidados de PasswordResetService
    public void createResetRequest(String email, String password) {
        // Stub - funcionalidad no core
    }
    
    public boolean processResetRequest(String token) {
        // Stub - funcionalidad no core
        return false;
    }
}
