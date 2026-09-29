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
import com.residuosolido.app.event.RequestStatusChangedEvent;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.TrackingCode;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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

    private final RequestRepository requestRepository;
    private final LocalImageService imageService;
    private final CityOrgService cityOrgService;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestValidator validator;

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          CityOrgService cityOrgService,
                          ApplicationEventPublisher eventPublisher,
                          RequestValidator validator) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.cityOrgService = cityOrgService;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
    }

    // ========== Crear ==========

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String organizationId) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED);
        }
        validator.validateCreate(user, city, address, materials, organizationId);

        Request request = Request.forCitizen(user);
        request.updateDraft(city, address, addressReference, materials);

        Organization org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validator.validateMaterials(org, request.getMaterials());
        request.assignOrganization(org);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String organizationId,
                                            MultipartFile imageFile) {
        imageService.validateImage(imageFile);

        Request request = createRequest(user, city, address, addressReference, materials, organizationId);

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

        Organization org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
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
    //
    // Sin @Transactional a propósito: no hay ningún PlatformTransactionManager
    // configurado en el proyecto (verificado — 0 beans, RequestService no es
    // un proxy transaccional), así que la anotación que tenían estos métodos
    // antes no hacía nada real. No hace falta agregar uno: cada transición
    // hace una sola escritura a un único documento Mongo (atómica por
    // diseño del motor), que es exactamente la garantía que estos métodos
    // necesitan. Ver docs/TRADEOFFS.md §36.

    public void acceptRequest(String id, Organization org, TimeSlot slot) {
        if (org == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        logger.info("REQUEST_ACCEPT_STARTED: id={}, orgId={}, slot={}", id, org.getId(), slot);
        try {
            Request request = getOwnedOrgRequest(id, org);
            request.accept(slot);
            saveWithOptimisticLock(request);
            logger.info("REQUEST_ACCEPT_SAVED: id={}, newStatus={}", id, request.getStatus());
            eventPublisher.publishEvent(new RequestStatusChangedEvent(request, NotificationType.ACCEPTED));
            logger.info("REQUEST_ACCEPT_SUCCESS: id={}, notification event published", id);
        } catch (Exception e) {
            logger.error("REQUEST_ACCEPT_FAILED: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void rejectRequest(String id, Organization org) {
        if (org == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        logger.info("REQUEST_REJECT_STARTED: id={}, orgId={}", id, org.getId());
        try {
            Request request = getOwnedOrgRequest(id, org);
            request.reject();
            saveWithOptimisticLock(request);
            logger.info("REQUEST_REJECT_SAVED: id={}, newStatus={}", id, request.getStatus());
            eventPublisher.publishEvent(new RequestStatusChangedEvent(request, NotificationType.REJECTED));
            logger.info("REQUEST_REJECT_SUCCESS: id={}, notification event published", id);
        } catch (Exception e) {
            logger.error("REQUEST_REJECT_FAILED: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
    }

    public void completeRequest(String id, Organization org) {
        if (org == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        logger.info("REQUEST_COMPLETE_STARTED: id={}, orgId={}", id, org.getId());
        try {
            Request request = getOwnedOrgRequest(id, org);
            request.complete();
            saveWithOptimisticLock(request);
            logger.info("REQUEST_COMPLETE_SUCCESS: id={}, newStatus={}", id, request.getStatus());
        } catch (Exception e) {
            logger.error("REQUEST_COMPLETE_FAILED: id={}, error={}", id, e.getMessage(), e);
            throw e;
        }
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
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return requestRepository.findByUser(user, PageRequest.of(page, size));
    }

    public Request getOwnedRequest(String id, User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
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
    // ========== Consultas: organización ==========

    public Request getOwnedOrgRequest(String id, Organization org) {
        if (org == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.FLASH_ORG_REQUEST_NOT_FOUND));
        if (request.getOrganization() == null || !request.getOrganization().getId().equals(org.getId())) {
            throw new OwnershipException(ServerMessage.FLASH_ORG_REQUEST_NOT_OWNED);
        }
        return request;
    }

    public List<Request> getRequestsByOrganization(Organization organization, int page, int size) {
        if (organization == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization,
                PageRequest.of(page, size));
    }

    public List<Request> getOrgRequestsByStatusFilter(Organization organization, String status, int page, int size) {
        if (organization == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
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

    // Overloads para compatibilidad retroactiva con tests (ignoran parámetros de guest)
    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone,
                                  String organizationId) {
        return createRequest(user, city, address, addressReference, materials, organizationId);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId, MultipartFile imageFile) {
        return createRequestWithImage(user, city, address, addressReference, materials, organizationId, imageFile);
    }
}
