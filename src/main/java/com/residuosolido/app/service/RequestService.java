package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
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

    @Autowired
    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
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
}
