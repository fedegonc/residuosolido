package com.residuosolido.app.service;

import com.residuosolido.app.model.PasswordResetRequest;
import com.residuosolido.app.repository.PasswordResetRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PasswordResetService {
    
    @Autowired
    private PasswordResetRequestRepository repository;
    
    public void createResetRequest(String maskedEmail, String lastKnownPassword) {
        PasswordResetRequest request = new PasswordResetRequest(maskedEmail, lastKnownPassword);
        repository.save(request);
    }
    
    public List<PasswordResetRequest> getPendingRequests() {
        return repository.findByStatusOrderByRequestDateDesc(PasswordResetRequest.Status.PENDING);
    }
    
    public void approveRequest(Long id, String adminNotes) {
        PasswordResetRequest request = repository.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(PasswordResetRequest.Status.APPROVED);
            request.setAdminNotes(adminNotes);
            repository.save(request);
        }
    }
    
    public void rejectRequest(Long id, String adminNotes) {
        PasswordResetRequest request = repository.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(PasswordResetRequest.Status.REJECTED);
            request.setAdminNotes(adminNotes);
            repository.save(request);
        }
    }
}
