package com.residuosolido.app.service;

import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.stereotype.Service;

@Service
public class RequestTransitionService {

    private final RequestRepository requestRepository;
    private final NotificationService notificationService;
    private final RequestOrganizationService requestOrganizationService;

    public RequestTransitionService(RequestRepository requestRepository,
                                    NotificationService notificationService,
                                    RequestOrganizationService requestOrganizationService) {
        this.requestRepository = requestRepository;
        this.notificationService = notificationService;
        this.requestOrganizationService = requestOrganizationService;
    }

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = requestOrganizationService.getOwnedOrgRequest(id, org);
        request.accept(slot);
        requestRepository.save(request);
        notifyRequestAccepted(request, slot);
    }

    public void rejectRequest(String id, User org) {
        Request request = requestOrganizationService.getOwnedOrgRequest(id, org);
        request.reject();
        requestRepository.save(request);
        notifyRequestRejected(request);
    }

    public void completeRequest(String id, User org) {
        Request request = requestOrganizationService.getOwnedOrgRequest(id, org);
        request.complete();
        requestRepository.save(request);
        notifyRequestCompleted(request);
    }

    private void notifyRequestAccepted(Request request, TimeSlot slot) {
        String orgName = request.getOrganization() != null ? request.getOrganization().getDisplayName() : "la organización";
        String materials = request.getMaterials() != null && !request.getMaterials().isEmpty()
                ? request.getMaterials().toString() : "a coordinar";
        notifyUser(request, "Su solicitud fue aceptada por " + orgName
                + ". Horario confirmado: " + slot + ". Materiales: " + materials + ".");
    }

    private void notifyRequestRejected(Request request) {
        String orgName = request.getOrganization() != null ? request.getOrganization().getDisplayName() : "la organización";
        notifyUser(request, "Su solicitud fue rechazada por " + orgName + ".");
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
