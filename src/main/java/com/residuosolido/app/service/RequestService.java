package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquesta la creación de solicitudes de recolección.
 * Valida campos, asigna la organización correspondiente a la ciudad seleccionada
 * y persiste la solicitud en estado PENDING.
 *
 * Nota de diseño: la imagen se valida ANTES de persistir la solicitud, de modo que
 * una imagen inválida no deje una solicitud huérfana en la base de datos.
 */
@Service
public class RequestService {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int TRACKING_CODE_LENGTH = 8;
    private static final char[] TRACKING_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final RequestRepository requestRepository;
    private final LocalImageService imageService;
    private final RequestValidator validator;
    private final CityOrgService cityOrgService;

    public RequestService(RequestRepository requestRepository,
                          LocalImageService imageService,
                          RequestValidator validator,
                          CityOrgService cityOrgService) {
        this.requestRepository = requestRepository;
        this.imageService = imageService;
        this.validator = validator;
        this.cityOrgService = cityOrgService;
    }

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone,
                                  String organizationId, String estimatedWeight, String estimatedVolume) {
        validator.validateCreate(user, city, address, materials, guestName, guestPhone, organizationId);
        validator.validateEstimates(estimatedWeight, estimatedVolume);

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
        validator.validateMaterials(org, request.getMaterials());
        request.assignOrganization(org);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId, String estimatedWeight, String estimatedVolume,
                                            MultipartFile imageFile) {
        // Validar la imagen ANTES de persistir la solicitud: una imagen inválida
        // no debe dejar una solicitud huérfana en la base de datos.
        imageService.validateImage(imageFile);

        Request request = createRequest(user, city, address, addressReference, materials,
                guestName, guestPhone, organizationId, estimatedWeight, estimatedVolume);

        if (imageFile != null && !imageFile.isEmpty()) {
            return imageService.attachImageToRequest(request, imageFile);
        }
        return request;
    }

    private String generateTrackingCode() {
        StringBuilder sb = new StringBuilder(TRACKING_CODE_LENGTH);
        for (int i = 0; i < TRACKING_CODE_LENGTH; i++) {
            sb.append(TRACKING_ALPHABET[RNG.nextInt(TRACKING_ALPHABET.length)]);
        }
        return sb.toString();
    }
}
