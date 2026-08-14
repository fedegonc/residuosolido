package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class RequestUpdateService {

    private final RequestRepository requestRepository;
    private final RequestQueryService requestQueryService;
    private final RequestValidator validator;
    private final OrgResolver orgResolver;
    private final LocalImageService imageService;

    public RequestUpdateService(RequestRepository requestRepository,
                                RequestQueryService requestQueryService,
                                RequestValidator validator,
                                OrgResolver orgResolver,
                                LocalImageService imageService) {
        this.requestRepository = requestRepository;
        this.requestQueryService = requestQueryService;
        this.validator = validator;
        this.orgResolver = orgResolver;
        this.imageService = imageService;
    }

    public Request updateRequest(String id, User user, City city, String address,
                                  String addressReference, List<MaterialCategory> materials,
                                  MultipartFile imageFile) {
        Request request = requestQueryService.getEditableOwnedRequest(id, user);
        validator.validateUpdate(city, address, materials);
        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        User newOrg = orgResolver.reassignIfNeeded(request.getOrganization(), city);
        if (!newOrg.equals(request.getOrganization())) {
            request.assignOrganization(newOrg);
        }
        request = requestRepository.save(request);
        return imageService.attachImageToRequest(request, imageFile);
    }

    public void deleteOwnedRequest(String id, User user) {
        requestQueryService.getOwnedRequest(id, user);
        requestRepository.deleteById(id);
    }
}
