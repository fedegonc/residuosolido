package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    public Request createRequest(User user, String description, String materials) {
        Request request = new Request();
        // Setters básicos sin Lombok
        request.setUser(user);
        request.setDescription(description);
        request.setMaterials(materials);
        request.setStatus(Request.RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        return requestRepository.save(request);
    }

    public List<Request> getRequestsByUser(User user) {
        return requestRepository.findByUser(user);
    }

    public List<Request> findAll() {
        return requestRepository.findAll();
    }
    
    public List<Request> getPendingRequests() {
        return requestRepository.findByStatus(Request.RequestStatus.PENDING);
    }

    public Request approveRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus(Request.RequestStatus.ACCEPTED);
            return requestRepository.save(request);
        }
        return null;
    }

    public Request rejectRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus(Request.RequestStatus.REJECTED);
            return requestRepository.save(request);
        }
        return null;
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public void deleteById(Long id) {
        requestRepository.deleteById(id);
    }
}
