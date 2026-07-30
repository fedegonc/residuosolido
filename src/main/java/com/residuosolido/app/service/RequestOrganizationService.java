package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RequestOrganizationService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestOrganizationService.class);

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;

    @Autowired
    public RequestOrganizationService(RequestRepository requestRepository, NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
    }

    public Request getOwnedOrgRequest(String id, User org) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.org.request_not_found"));
        if (request.getOrganization() == null || !request.getOrganization().getId().equals(org.getId())) {
            throw new SecurityException("flash.org.request_not_owned");
        }
        return request;
    }

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = getOwnedOrgRequest(id, org);
        request.accept(slot);
        requestRepository.save(request);
        notifyRequestAccepted(request, slot);
    }

    public void rejectRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.reject();
        requestRepository.save(request);
        notifyRequestRejected(request);
    }

    public void completeRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.complete();
        requestRepository.save(request);
        notifyRequestCompleted(request);
    }

    public List<Request> getRequestsByOrganization(User organization, int page, int size) {
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization,
                org.springframework.data.domain.PageRequest.of(page, size));
    }

    public List<Request> getRequestsByOrganizationAndStatus(User organization, RequestStatus status) {
        return requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, status, Pageable.unpaged());
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
            return getRequestsByOrganizationAndStatus(organization, filterStatus);
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de status inválido ignorado: {}", status);
            return getRequestsByOrganization(organization, page, size);
        }
    }

    private void notifyRequestAccepted(Request request, TimeSlot slot) {
        String orgName = request.getOrganization() != null ? request.getOrganization().getDisplayName() : "la organización";
        String materials = request.getMaterials() != null && !request.getMaterials().isEmpty()
                ? request.getMaterials().toString()
                : "a coordinar";
        notifyUser(request, "Su solicitud fue aceptada por " + orgName
                + ". Horario confirmado: " + slot
                + ". Materiales: " + materials + ".");
    }

    private void notifyRequestRejected(Request request) {
        String orgName = request.getOrganization() != null ? request.getOrganization().getDisplayName() : "la organización";
        notifyUser(request, "Su solicitud fue rechazada por " + orgName + ". Contacte a la organización para más información.");
    }

    private void notifyRequestCompleted(Request request) {
        String orgName = request.getOrganization() != null ? request.getOrganization().getDisplayName() : "la organización";
        notifyUser(request, "Su solicitud fue completada por " + orgName + ". Gracias por reciclar.");
    }

    private void notifyUser(Request request, String message) {
        String phone = request.getContactPhone();
        if (phone != null && !phone.isBlank()) {
            notificationService.sendWhatsApp(phone, message);
        }
    }
}
