package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestQueryService {

    private final RequestRepository requestRepository;

    public RequestQueryService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public List<Request> getRequestsByUser(User user, int page, int size) {
        return requestRepository.findByUser(user, PageRequest.of(page, size));
    }

    public List<Request> getRecentRequestsByUser(User user, int limit) {
        return requestRepository.findByUser(user, PageRequest.of(0, limit));
    }

    public Request getOwnedRequest(String id, User user) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.request.not_found"));
        if (request.getUser() == null || !request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("flash.request.not_owned");
        }
        return request;
    }

    public Request getEditableOwnedRequest(String id, User user) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
        return request;
    }

    public List<Request> getGuestRequestsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        return requestRepository.findByGuestPhoneOrderByCreatedAtDesc(phone.trim());
    }
}
