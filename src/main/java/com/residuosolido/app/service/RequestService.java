package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
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
import java.time.LocalDateTime;
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

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          CityOrgService cityOrgService) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.cityOrgService = cityOrgService;
    }

    // ========== Crear ==========

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone,
                                  String organizationId, String estimatedWeight, String estimatedVolume) {
        validateCreate(user, city, address, materials, guestName, guestPhone, organizationId);
        validateEstimates(estimatedWeight, estimatedVolume);

        Request request = new Request();
        if (user != null) {
            request.setUser(user);
        } else {
            request.setGuestName(guestName);
            request.setGuestPhone(guestPhone);
            request.setTrackingCode(generateTrackingCode());
        }
        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        request.setEstimatedWeight(estimatedWeight);
        request.setEstimatedVolume(estimatedVolume);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validateMaterials(org, request.getMaterials());
        request.assignOrganization(org);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId, String estimatedWeight, String estimatedVolume,
                                            MultipartFile imageFile) {
        imageService.validateImage(imageFile);

        Request request = createRequest(user, city, address, addressReference, materials,
                guestName, guestPhone, organizationId, estimatedWeight, estimatedVolume);

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
        validateUpdate(city, address, materials, organizationId);

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validateMaterials(org, materials != null ? materials : List.of());

        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        request.assignOrganization(org);
        request = requestRepository.save(request);
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
            throw new IllegalStateException("flash.request.delete.concurrent", ex);
        }
    }

    // ========== Transiciones de estado (organización) ==========

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = getOwnedOrgRequest(id, org);
        request.accept(slot);
        saveWithOptimisticLock(request);
    }

    public void rejectRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.reject();
        saveWithOptimisticLock(request);
    }

    public void completeRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.complete();
        saveWithOptimisticLock(request);
    }

    private void saveWithOptimisticLock(Request request) {
        try {
            requestRepository.save(request);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("flash.request.concurrent_modification", e);
        }
    }

    // ========== Consultas: ciudadano ==========

    public List<Request> getRequestsByUser(User user, int page, int size) {
        return requestRepository.findByUser(user, PageRequest.of(page, size));
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
        String canonicalPhone = PhoneNumber.normalize(phone);
        return requestRepository
                .findByGuestPhoneAndTrackingCodeOrderByCreatedAtDesc(canonicalPhone, trackingCode.trim());
    }

    // ========== Consultas: organización ==========

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

    // ========== Validación ==========

    public void validateCreate(User user, City city, String address,
                                List<MaterialCategory> materials, String guestName, String guestPhone,
                                String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
        if (user == null) {
            validateGuest(guestName, guestPhone);
        } else {
            if (!user.isActive() || user.getRole() != Role.USER) {
                throw new IllegalArgumentException("error.request.citizen_required");
            }
            if (!PhoneNumber.isValid(user.getPhone())) {
                throw new IllegalArgumentException("error.profile.phone_required");
            }
        }
    }

    public void validateUpdate(City city, String address, List<MaterialCategory> materials, String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
    }

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials, String organizationId) {
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.address_required");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("error.request.materials_required");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("error.request.organization_required");
        }
    }

    public void validateMaterials(User organization, List<MaterialCategory> materials) {
        if (organization.getAcceptedMaterials() == null || materials == null || materials.isEmpty()
                || materials.stream().anyMatch(m -> m == null || !organization.getAcceptedMaterials().contains(m))) {
            throw new IllegalArgumentException("error.request.materials_not_accepted");
        }
    }

    public void validateEstimates(String weight, String volume) {
        if (weight != null && !weight.isBlank() && !List.of("0-5", "5-20", "20-50", "50+").contains(weight)) {
            throw new IllegalArgumentException("error.request.invalid_weight");
        }
        if (volume != null && !volume.isBlank() && !List.of("bag", "box", "trunk", "pickup").contains(volume)) {
            throw new IllegalArgumentException("error.request.invalid_volume");
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new IllegalArgumentException("error.name.required");
        }
        if (guestName.trim().length() > 100) {
            throw new IllegalArgumentException("error.name.too_long");
        }
        PhoneNumber.normalize(guestPhone);
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
