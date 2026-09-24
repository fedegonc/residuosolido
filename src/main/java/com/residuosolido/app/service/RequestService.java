package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.OwnershipException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

/**
 * Servicio unificado de solicitudes: consultas, creación/edición/eliminación y transiciones de estado.
 * Unifica lo que antes era RequestService + RequestQueryService + RequestTransitionService.
 */
@Service
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    private static final SecureRandom RNG = new SecureRandom();
    private static final int TRACKING_CODE_LENGTH = 8;
    private static final char[] TRACKING_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final RequestRepository requestRepository;
    private final LocalImageService imageService;
    private final CityOrgService cityOrgService;
    private final NotificationService notificationService;
    private final RequestValidator validator;
    private final RequestStateMachine stateMachine;

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          CityOrgService cityOrgService,
                          NotificationService notificationService,
                          RequestValidator validator,
                          RequestStateMachine stateMachine) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.cityOrgService = cityOrgService;
        this.notificationService = notificationService;
        this.validator = validator;
        this.stateMachine = stateMachine;
    }

    // ========== Crear ==========

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone,
                                  String organizationId) {
        validator.validateCreate(user, city, address, materials, guestName, guestPhone, organizationId);

        Request request = user != null
                ? Request.forCitizen(user)
                : Request.forGuest(guestName, guestPhone, generateTrackingCode());
        request.updateDraft(city, address, addressReference, materials);

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validator.validateMaterials(org, request.getMaterials());
        request.assignOrganization(org);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId,
                                            MultipartFile imageFile) {
        imageService.validateImage(imageFile);

        Request request = createRequest(user, city, address, addressReference, materials,
                guestName, guestPhone, organizationId);

        if (imageFile != null && !imageFile.isEmpty()) {
            return imageService.attachImageToRequest(request, imageFile);
        }
        return request;
    }

    // ========== Editar ==========

    public Request updateRequest(String id, User user, City city, String address,
                                  String addressReference, List<MaterialCategory> materials,
                                  String organizationId, MultipartFile imageFile) {
        Request request = getEditableOwnedRequest(id, user);
        validator.validateUpdate(city, address, materials, organizationId);

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validator.validateMaterials(org, materials != null ? materials : List.of());

        request.updateDraft(city, address, addressReference, materials);
        request.assignOrganization(org);
        request = saveWithOptimisticLock(request);
        return imageService.attachImageToRequest(request, imageFile);
    }

    // ========== Eliminar ==========

    /**
     * Borra la solicitud del propietario. Requiere que la solicitud siga siendo
     * editable (PENDING) y pertenezca al usuario. El borrado se ejecuta sobre la
     * entidad cargada con su versión, de modo que si una transición concurrente
     * (aceptación por la organización) la modificó, el borrado falle con
     * OptimisticLockingFailureException en lugar de sobrescribir el estado.
     */
    public void deleteOwnedRequest(String id, User user) {
        Request request = getEditableOwnedRequest(id, user);
        try {
            requestRepository.delete(request);
        } catch (OptimisticLockingFailureException ex) {
            throw new StateException(ServerMessage.FLASH_REQUEST_DELETE_CONCURRENT, ex);
        }
    }

    // ========== Transiciones de estado (organización) ==========

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = getOwnedOrgRequest(id, org);
        stateMachine.accept(request, slot);
        saveWithOptimisticLock(request);
        // Después del save: si la transición falla por concurrencia, no se
        // notifica un estado que no quedó persistido.
        notificationService.notifyRequester(request, NotificationType.ACCEPTED);
    }

    public void rejectRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        stateMachine.reject(request);
        saveWithOptimisticLock(request);
        notificationService.notifyRequester(request, NotificationType.REJECTED);
    }

    public void completeRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        stateMachine.complete(request);
        saveWithOptimisticLock(request);
    }

    private Request saveWithOptimisticLock(Request request) {
        try {
            return requestRepository.save(request);
        } catch (OptimisticLockingFailureException e) {
            throw new StateException(ServerMessage.FLASH_REQUEST_CONCURRENT_MODIFICATION, e);
        }
    }

    // ========== Consultas: ciudadano ==========

    public List<Request> getRequestsByUser(User user, int page, int size) {
        return requestRepository.findByUser(user, PageRequest.of(page, size));
    }

    public Request getOwnedRequest(String id, User user) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.FLASH_REQUEST_NOT_FOUND));
        if (request.getUser() == null || !request.getUser().getId().equals(user.getId())) {
            throw new OwnershipException(ServerMessage.FLASH_REQUEST_NOT_OWNED);
        }
        return request;
    }

    public Request getEditableOwnedRequest(String id, User user) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new StateException(ServerMessage.FLASH_REQUEST_EDIT_PENDING_ONLY);
        }
        return request;
    }

    // ========== Consultas: invitado ==========

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
        // Formato de telefono invalido -> mismo resultado que "no encontrado", no una
        // excepcion que se propague. Bug real reportado por el usuario: un invitado
        // (siempre anonimo) que llegaba aca con un telefono mal formado terminaba
        // rebotado a /entrar por el manejador generico de errores basado en rol —
        // no tiene sentido mandar a loguearse a alguien que nunca tuvo cuenta.
        String canonicalPhone;
        try {
            canonicalPhone = PhoneNumber.normalize(phone);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return requestRepository
                .findByGuestPhoneAndTrackingCodeOrderByCreatedAtDesc(canonicalPhone, trackingCode.trim());
    }

    // ========== Consultas: organización ==========

    public Request getOwnedOrgRequest(String id, User org) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.FLASH_ORG_REQUEST_NOT_FOUND));
        if (request.getOrganization() == null || !request.getOrganization().getId().equals(org.getId())) {
            throw new OwnershipException(ServerMessage.FLASH_ORG_REQUEST_NOT_OWNED);
        }
        return request;
    }

    public List<Request> getRequestsByOrganization(User organization, int page, int size) {
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization,
                PageRequest.of(page, size));
    }

    public List<Request> getOrgRequestsByStatusFilter(User organization, String status, int page, int size) {
        if (status == null || status.trim().isEmpty()) {
            return getRequestsByOrganization(organization, page, size);
        }
        try {
            RequestStatus filterStatus = RequestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            return requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, filterStatus, PageRequest.of(page, size));
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de status inválido ignorado: {}", status);
            return getRequestsByOrganization(organization, page, size);
        }
    }

    // ========== Util ==========

    private String generateTrackingCode() {
        StringBuilder sb = new StringBuilder(TRACKING_CODE_LENGTH);
        for (int i = 0; i < TRACKING_CODE_LENGTH; i++) {
            sb.append(TRACKING_ALPHABET[RNG.nextInt(TRACKING_ALPHABET.length)]);
        }
        return sb.toString();
    }
}
