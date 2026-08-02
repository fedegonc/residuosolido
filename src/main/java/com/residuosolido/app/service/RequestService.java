package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final CityOrganizationService cityOrganizationService;
    private final LocalImageService imageService;

    @Autowired
    public RequestService(RequestRepository requestRepository,
                          CityOrganizationService cityOrganizationService,
                          LocalImageService imageService) {
        this.requestRepository = requestRepository;
        this.cityOrganizationService = cityOrganizationService;
        this.imageService = imageService;
    }

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone,
                                  String organizationId, String estimatedWeight, String estimatedVolume) {
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.address_required");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("error.request.materials_required");
        }
        if (user == null) {
            if (guestName == null || guestName.trim().isEmpty()) {
                throw new IllegalArgumentException("error.request.guest_name_required");
            }
            if (guestPhone == null || guestPhone.trim().isEmpty()) {
                throw new IllegalArgumentException("error.request.guest_phone_required");
            }
            if (!guestPhone.matches("^[+][0-9]{1,3}[\\s0-9]{6,15}$")) {
                throw new IllegalArgumentException("error.request.invalid_phone");
            }
        }
        Request request = new Request();
        if (user != null) {
            request.setUser(user);
        } else {
            request.setGuestName(guestName);
            request.setGuestPhone(guestPhone);
        }
        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        request.setEstimatedWeight(estimatedWeight);
        request.setEstimatedVolume(estimatedVolume);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        User org;
        if (organizationId != null && !organizationId.isBlank()) {
            org = cityOrganizationService.findOrganizationByIdAndCity(organizationId, city);
        } else {
            List<User> orgs = cityOrganizationService.getOrganizationsByCity(city);
            if (orgs == null || orgs.isEmpty()) {
                throw new IllegalArgumentException("error.request.no_organization");
            }
            org = orgs.get(0);
        }
        request.assignOrganization(org);
        logger.info("Organización auto-asignada: {} para ciudad: {}", org.getDisplayName(), city);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId, String estimatedWeight, String estimatedVolume,
                                            MultipartFile imageFile) {
        Request request = createRequest(user, city, address, addressReference, materials, guestName, guestPhone, organizationId, estimatedWeight, estimatedVolume);
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = imageService.uploadFile(imageFile);
                request.setImageUrl(imageUrl);
                request = requestRepository.save(request);
            } catch (Exception e) {
                logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
                throw new IllegalStateException("flash.request.image_upload_failed", e);
            }
        }
        return request;
    }

    public List<Request> getRequestsByUser(User user, int page, int size) {
        return requestRepository.findByUser(user, org.springframework.data.domain.PageRequest.of(page, size));
    }

    public List<Request> getRecentRequestsByUser(User user, int limit) {
        return requestRepository.findByUser(user, org.springframework.data.domain.PageRequest.of(0, limit));
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

    public void deleteOwnedRequest(String id, User user) {
        getOwnedRequest(id, user);
        requestRepository.deleteById(id);
    }

    public Request updateRequest(String id, User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, MultipartFile imageFile) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.address_required");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("error.request.materials_required");
        }
        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        request = requestRepository.save(request);
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                request.setImageUrl(imageService.uploadFile(imageFile));
                request = requestRepository.save(request);
            } catch (Exception e) {
                logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
                throw new IllegalStateException("flash.request.image_upload_failed", e);
            }
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
