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
    private final RequestValidator validator;
    private final OrganizationResolver orgResolver;
    private final LocalImageService imageService;

    public RequestUpdateService(RequestRepository requestRepository,
                                RequestValidator validator,
                                OrganizationResolver orgResolver,
                                LocalImageService imageService) {
        this.requestRepository = requestRepository;
        this.validator = validator;
        this.orgResolver = orgResolver;
        this.imageService = imageService;
    }

    public Request updateRequest(String id, User user, City city, String address,
                                  String addressReference, List<MaterialCategory> materials,
                                  MultipartFile imageFile) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.request.not_found"));
        if (request.getUser() == null || !request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("flash.request.not_owned");
        }
        if (!request.canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
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
        return attachImage(request, imageFile);
    }

    public void deleteOwnedRequest(String id, User user) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.request.not_found"));
        if (request.getUser() == null || !request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("flash.request.not_owned");
        }
        requestRepository.deleteById(id);
    }

    private Request attachImage(Request request, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                request.setImageUrl(imageService.uploadFile(imageFile));
                return requestRepository.save(request);
            } catch (Exception e) {
                throw new IllegalStateException("flash.request.image_upload_failed", e);
            }
        }
        return request;
    }
}
