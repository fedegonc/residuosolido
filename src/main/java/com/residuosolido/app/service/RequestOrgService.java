package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestOrgService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestOrgService.class);

    private final RequestRepository requestRepository;

    @Autowired
    public RequestOrgService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public Request getOwnedOrgRequest(String id, User org) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.org.request_not_found"));
        if (request.getOrganization() == null || !request.getOrganization().getId().equals(org.getId())) {
            throw new SecurityException("flash.org.request_not_owned");
        }
        return request;
    }

    public List<Request> getRequestsByOrganization(User organization, int page, int size) {
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization,
                org.springframework.data.domain.PageRequest.of(page, size));
    }

    public List<Request> getRequestsByOrganizationAndStatus(User organization, RequestStatus status, int page, int size) {
        return requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, status, PageRequest.of(page, size));
    }

    public List<Request> getRecentPendingRequestsByOrganization(User organization, int limit) {
        return requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, RequestStatus.PENDING, PageRequest.of(0, limit));
    }

    public List<Request> getOrgRequestsByStatusFilter(User organization, String status, int page, int size) {
        if (status == null || status.trim().isEmpty()) {
            return getRequestsByOrganization(organization, page, size);
        }
        try {
            RequestStatus filterStatus = RequestStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
            return getRequestsByOrganizationAndStatus(organization, filterStatus, page, size);
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de status inválido ignorado: {}", status);
            return getRequestsByOrganization(organization, page, size);
        }
    }
}
