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
    
    // Cache simple para evitar queries repetidas
    private List<PasswordResetRequest> cachedPendingRequests = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION_MS = 30000; // 30 segundos
    
    public void createResetRequest(String maskedEmail, String lastKnownPassword) {
        PasswordResetRequest request = new PasswordResetRequest(maskedEmail, lastKnownPassword);
        repository.save(request);
        cachedPendingRequests = null; // Invalidar cache al crear una nueva solicitud
    }
    
    public List<PasswordResetRequest> getPendingRequests() {
        long currentTime = System.currentTimeMillis();
        if (cachedPendingRequests == null || currentTime - lastCacheUpdate > CACHE_DURATION_MS) {
            cachedPendingRequests = repository.findByStatusOrderByRequestDateDesc(PasswordResetRequest.Status.PENDING);
            lastCacheUpdate = currentTime;
        }
        return cachedPendingRequests;
    }
    
    public void approveRequest(Long id, String adminNotes) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus(PasswordResetRequest.Status.APPROVED);
            request.setAdminNotes(adminNotes);
            repository.save(request);
            cachedPendingRequests = null; // Invalidar cache
        });
    }
    
    public void rejectRequest(Long id, String adminNotes) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus(PasswordResetRequest.Status.REJECTED);
            request.setAdminNotes(adminNotes);
            repository.save(request);
            cachedPendingRequests = null; // Invalidar cache
        });
    }
}
