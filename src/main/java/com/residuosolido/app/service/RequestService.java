package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.exception.StateException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.event.RequestStatusChangedEvent;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final MongoTemplate mongoTemplate;

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          CityOrgService cityOrgService,
                          ApplicationEventPublisher eventPublisher,
                          RequestValidator validator,
                          MongoTemplate mongoTemplate) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.cityOrgService = cityOrgService;
        this.eventPublisher = eventPublisher;
        this.validator = validator;
        this.mongoTemplate = mongoTemplate;
    }

    // ========== Crear ==========

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String organizationId) {
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
        validator.requireOrganization(org);
        executeTransition(id, org, "ACCEPT", request -> request.accept(slot), NotificationType.ACCEPTED);
    }

    public void rejectRequest(String id, Organization org) {
        validator.requireOrganization(org);
        executeTransition(id, org, "REJECT", Request::reject, NotificationType.REJECTED);
    }

    public void completeRequest(String id, Organization org) {
        validator.requireOrganization(org);
        executeTransition(id, org, "COMPLETE", Request::complete, null);
    }

    private void executeTransition(String id, Organization org, String action,
                                   java.util.function.Consumer<Request> transition,
                                   NotificationType eventTypeOrNull) {
        logger.info("REQUEST_{}_STARTED: id={}, orgId={}", action, id, org.getId());
        try {
            Request request = getOwnedOrgRequest(id, org);
            transition.accept(request);
            saveWithOptimisticLock(request);
            logger.info("REQUEST_{}_SAVED: id={}, newStatus={}", action, id, request.getStatus());
            if (eventTypeOrNull != null) {
                eventPublisher.publishEvent(new RequestStatusChangedEvent(request, eventTypeOrNull));
                logger.info("REQUEST_{}_SUCCESS: id={}, notification event published", action, id);
            } else {
                logger.info("REQUEST_{}_SUCCESS: id={}", action, id);
            }
        } catch (RuntimeException e) {
            logger.error("REQUEST_{}_FAILED: id={}, error={}", action, id, e.getMessage(), e);
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
        validator.requireUser(user);
        List<Request> requests = requestRepository.findByUser(user, PageRequest.of(page, size));
        requests.forEach(request -> request.setContactUser(user));
        return requests;
    }

    public Request getOwnedRequest(String id, User user) {
        validator.requireUser(user);
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.FLASH_REQUEST_NOT_FOUND));
        validator.requireOwnedByCitizen(request, user);
        return request;
    }

    public Request getEditableOwnedRequest(String id, User user) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new StateException(ServerMessage.FLASH_REQUEST_EDIT_PENDING_ONLY);
        }
        return request;
    }

    // ========== Consultas: organización ==========

    public Request getOwnedOrgRequest(String id, Organization org) {
        validator.requireOrganization(org);
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.FLASH_ORG_REQUEST_NOT_FOUND));
        validator.requireOwnedByOrganization(request, org);
        return request;
    }

    public List<Request> getRequestsByOrganization(Organization organization, int page, int size) {
        validator.requireOrganization(organization);
        return withHydratedUsers(requestRepository.findByOrganizationOrderByCreatedAtDesc(organization,
                PageRequest.of(page, size)));
    }

    public List<Request> getOrgRequestsByStatusFilter(Organization organization, String status, int page, int size) {
        validator.requireOrganization(organization);
        if (status == null || status.trim().isEmpty()) {
            return getRequestsByOrganization(organization, page, size);
        }
        try {
            RequestStatus filterStatus = RequestStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            return withHydratedUsers(requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, filterStatus, PageRequest.of(page, size)));
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de status inválido ignorado: {}", status);
            return getRequestsByOrganization(organization, page, size);
        }
    }

    /**
     * Reemplaza los proxies lazy de {@link Request#getUser()} por los Users
     * reales en una sola query batch. Sin esto, cada card del kanban resuelve
     * su ref con un findById propio al pedir {@code contactName} (N+1 medido:
     * 5 requests = 5 finds en users — ver RequestServiceHydrationTest).
     *
     * Costo fijo: 2 queries extra por lista (proyección de ids + fetch batch),
     * independiente de N. Los @DocumentReference(lazy) quedan intactos para
     * los paths que no son listas (detalle, FSM, eventos).
     */
    private List<Request> withHydratedUsers(List<Request> requests) {
        if (requests.isEmpty()) {
            return requests;
        }
        // _id es ObjectId en Mongo; la query cruda (Document.class) no convierte String.
        List<Object> requestIds = requests.stream()
                .map(r -> ObjectId.isValid(r.getId()) ? new ObjectId(r.getId()) : (Object) r.getId())
                .toList();
        Query refQuery = new Query(Criteria.where("_id").in(requestIds));
        refQuery.fields().include("user");
        List<Document> refs = mongoTemplate.find(refQuery, Document.class, "requests");

        Map<String, String> userIdByRequestId = new HashMap<>();
        Set<ObjectId> userIds = new LinkedHashSet<>();
        for (Document ref : refs) {
            Object uid = ref.get("user");
            if (uid instanceof ObjectId oid) {
                userIds.add(oid);
                userIdByRequestId.put(ref.getObjectId("_id").toHexString(), oid.toHexString());
            }
        }
        if (userIds.isEmpty()) {
            return requests;
        }
        Map<String, User> usersById = mongoTemplate
                .find(new Query(Criteria.where("_id").in(userIds)), User.class).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        for (Request r : requests) {
            User real = usersById.get(userIdByRequestId.get(r.getId()));
            if (real != null) {
                r.setContactUser(real);
            }
        }
        return requests;
    }

}
