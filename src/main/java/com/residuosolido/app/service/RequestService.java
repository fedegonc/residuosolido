package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.Name;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Operaciones del ciudadano sobre solicitudes: crear, editar, eliminar y validar.
 * Unifica lo que antes era RequestService + RequestValidator + RequestUpdateService.
 */
@Service
public class RequestService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TRACKING_CODE_LENGTH = 8;
    private static final char[] TRACKING_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final RequestRepository requestRepository;
    private final LocalImageService imageService;
    private final CityOrgService cityOrgService;
    private final RequestQueryService requestQueryService;

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          CityOrgService cityOrgService,
                          RequestQueryService requestQueryService) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.cityOrgService = cityOrgService;
        this.requestQueryService = requestQueryService;
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
        Request request = requestQueryService.getEditableOwnedRequest(id, user);
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
        Request request = requestQueryService.getEditableOwnedRequest(id, user);
        try {
            requestRepository.delete(request);
        } catch (OptimisticLockingFailureException ex) {
            throw new IllegalStateException("flash.request.delete.concurrent", ex);
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
        Name.of(guestName);
        PhoneNumber.of(guestPhone);
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
