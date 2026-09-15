package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Consultas de solicitudes: ciudadano, invitado y organización.
 * Unifica lo que antes era RequestQueryService + RequestOrgService.
 */
@Service
public class RequestQueryService {

    private static final Logger logger = LoggerFactory.getLogger(RequestQueryService.class);

    private final RequestRepository requestRepository;

    public RequestQueryService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // ========== Ciudadano ==========

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

    // ========== Invitado ==========

    /**
     * Busca solicitudes de invitado por teléfono + código privado de rastreo.
     * El teléfono solo NO es suficiente: cualquier persona podría conocerlo.
     * El código se entrega al invitado al crear la solicitud.
     */
    public List<Request> getGuestRequests(String phone, String trackingCode) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        if (trackingCode == null || trackingCode.isBlank()) {
            return List.of();
        }
        String canonicalPhone = com.residuosolido.app.model.PhoneNumber.of(phone).value();
        return requestRepository
                .findByGuestPhoneAndTrackingCodeOrderByCreatedAtDesc(canonicalPhone, trackingCode.trim());
    }

    // ========== Organización ==========

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
                PageRequest.of(page, size));
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
            RequestStatus filterStatus = RequestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            return getRequestsByOrganizationAndStatus(organization, filterStatus, page, size);
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de status inválido ignorado: {}", status);
            return getRequestsByOrganization(organization, page, size);
        }
    }

    /**
     * Agrupa las solicitudes de la organización por estado para la vista Kanban.
     * Devuelve un Map ordenado: PENDING, IN_PROGRESS, COMPLETED, REJECTED.
     */
    public Map<RequestStatus, List<Request>> getRequestsByOrganizationGroupedByStatus(User organization) {
        Map<RequestStatus, List<Request>> grouped = new LinkedHashMap<>();
        for (RequestStatus status : new RequestStatus[]{RequestStatus.PENDING, RequestStatus.IN_PROGRESS,
                RequestStatus.COMPLETED, RequestStatus.REJECTED}) {
            grouped.put(status, requestRepository
                    .findByOrganizationAndStatusOrderByCreatedAtDesc(organization, status, PageRequest.of(0, 100)));
        }
        return grouped;
    }
}
