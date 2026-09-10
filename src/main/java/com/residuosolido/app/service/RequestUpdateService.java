package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Actualización y borrado de solicitudes.
 *
 * Reglas:
 * - Solo el propietario autenticado puede editar/borrar (no aplica a invitados).
 * - Solo las solicitudes PENDING pueden editarse/borrarse.
 * - La edición revalida ciudad, dirección, materiales y organización, y verifica
 *   que los materiales sean aceptados por la organización asignada.
 * - El borrado usa deleteById sobre la solicitud previamente cargada con @Version,
 *   de modo que un cambio concurrente (aceptación por la organización) genere
 *   OptimisticLockingFailureException en lugar de un borrado silencioso.
 */
@Service
public class RequestUpdateService {

    private final RequestRepository requestRepository;
    private final RequestQueryService requestQueryService;
    private final RequestValidator validator;
    private final CityOrgService cityOrgService;
    private final LocalImageService imageService;

    public RequestUpdateService(RequestRepository requestRepository,
                                RequestQueryService requestQueryService,
                                RequestValidator validator,
                                CityOrgService cityOrgService,
                                LocalImageService imageService) {
        this.requestRepository = requestRepository;
        this.requestQueryService = requestQueryService;
        this.validator = validator;
        this.cityOrgService = cityOrgService;
        this.imageService = imageService;
    }

    public Request updateRequest(String id, User user, City city, String address,
                                  String addressReference, List<MaterialCategory> materials,
                                  String organizationId, MultipartFile imageFile) {
        Request request = requestQueryService.getEditableOwnedRequest(id, user);
        validator.validateUpdate(city, address, materials, organizationId);

        User org = cityOrgService.findOrganizationByIdAndCity(organizationId, city);
        validator.validateMaterials(org, materials != null ? materials : List.of());

        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        request.assignOrganization(org);
        request = requestRepository.save(request);
        return imageService.attachImageToRequest(request, imageFile);
    }

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
}
