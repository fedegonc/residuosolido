package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

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

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        request.assignOrganization(org);

        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            String organizationId, String estimatedWeight, String estimatedVolume,
                                            MultipartFile imageFile) {
        Request request = createRequest(user, city, address, addressReference, materials,
                guestName, guestPhone, organizationId, estimatedWeight, estimatedVolume);
        return imageService.attachImageToRequest(request, imageFile);
    }
}
