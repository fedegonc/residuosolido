package com.residuosolido.app.service;

import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para operaciones con la entidad Request (Solicitud)
 */
@Service
public class RequestService extends GenericEntityService<Request, Long> {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected JpaRepository<Request, Long> getRepository() {
        return requestRepository;
    }
    
    /**
     * Encuentra todas las solicitudes de un usuario específico
     */
    public List<Request> findByUser(User user) {
        return requestRepository.findByUserId(user.getId());
    }
    
    /**
     * Encuentra todas las solicitudes asignadas a una organización específica
     */
    public List<Request> findByOrganizationId(Long organizationId) {
        return requestRepository.findByOrganizationId(organizationId);
    }
    
    /**
     * Obtiene todas las organizaciones disponibles para solicitudes
     * @return Lista de organizaciones
     */
    public List<User> getOrganizations() {
        return userRepository.findByRole(com.residuosolido.app.model.Role.ORGANIZATION);
    }
    
    /**
     * Crea una nueva solicitud
     * @param organizationId ID de la organización (usuario con rol ORGANIZATION)
     * @param address Dirección de recolección
     * @param description Descripción/notas
     * @param currentUser Usuario que hace la solicitud
     * @return Solicitud creada
     */
    public Request createRequest(Long organizationId, String address, String description, User currentUser) {
        Request request = new Request();
        
        // Buscar la organización (usuario con rol ORGANIZATION)
        User organizationUser = userRepository.findById(organizationId).orElse(null);
        if (organizationUser != null && organizationUser.getRole() == com.residuosolido.app.model.Role.ORGANIZATION) {
            // Crear una organización temporal para la solicitud
            Organization org = new Organization();
            org.setId(organizationId);
            org.setName(organizationUser.getFirstName() + " " + organizationUser.getLastName());
            org.setEmail(organizationUser.getEmail());
            request.setOrganization(org);
        }
        
        request.setUser(currentUser);
        request.setCollectionAddress(address);
        request.setNotes(description);
        request.setStatus(Request.RequestStatus.PENDING);
        
        return save(request);
    }
}
