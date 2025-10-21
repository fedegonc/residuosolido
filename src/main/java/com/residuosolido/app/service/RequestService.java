package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.Material;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaterialRepository materialRepository;

    public Request createRequest(User user, String description, String materials, String address) {
        Request request = new Request();
        request.setUser(user);
        request.setDescription(description);
        
        // Parse materials string (comma-separated IDs) and convert to List<Material>
        List<Material> materialList = new ArrayList<>();
        if (materials != null && !materials.trim().isEmpty()) {
            String[] materialIds = materials.split(",");
            for (String idStr : materialIds) {
                try {
                    Long materialId = Long.parseLong(idStr.trim());
                    materialRepository.findById(materialId).ifPresent(materialList::add);
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        request.setMaterials(materialList);
        
        request.setAddress(address);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        return requestRepository.save(request);
    }

    public List<Request> getRecentRequestsByUser(User user) {
        return requestRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    public List<Request> getRecentRequestsByUser(User user, int limit) {
        return requestRepository.findByUser(user)
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByUser(User user) {
        List<Request> requests = requestRepository.findByUser(user);
        // Forzar la inicialización de relaciones lazy para evitar LazyInitializationException
        requests.forEach(request -> {
            if (request.getMaterials() != null) {
                request.getMaterials().size(); // Esto fuerza la carga de la colección
            }
            // Inicializar organización si existe
            if (request.getOrganization() != null) {
                request.getOrganization().getFullName(); // Fuerza la carga del proxy
            }
        });
        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> findAll() {
        List<Request> requests = requestRepository.findAll();
        // Forzar la inicialización de propiedades lazy del User y collections
        requests.forEach(request -> {
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getEmail();
            }
            // Forzar la inicialización de la organización
            if (request.getOrganization() != null) {
                request.getOrganization().getUsername();
                request.getOrganization().getFullName();
            }
            // Forzar la inicialización de la colección materials
            if (request.getMaterials() != null) {
                request.getMaterials().size(); // Esto fuerza la carga de la colección
            }
        });
        return requests;
    }

    public long count() {
        return requestRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<Request> findById(Long id) {
        Optional<Request> requestOpt = requestRepository.findById(id);
        // Forzar la inicialización de propiedades lazy si existe la request
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getEmail();
            }
            // Forzar la inicialización de la organización
            if (request.getOrganization() != null) {
                request.getOrganization().getUsername();
                request.getOrganization().getFullName();
            }
            // Forzar la inicialización de la colección materials
            if (request.getMaterials() != null) {
                request.getMaterials().size();
            }
        }
        return requestOpt;
    }

    public Request rejectRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus(RequestStatus.REJECTED);
            return requestRepository.save(request);
        }
        return null;
    }
    
    @Transactional(readOnly = true)
    public List<Request> getPendingRequests() {
        List<Request> requests = requestRepository.findByStatus(RequestStatus.PENDING);
        // Forzar la inicialización de propiedades lazy
        requests.forEach(request -> {
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getFullName();
            }
            if (request.getOrganization() != null) {
                request.getOrganization().getUsername();
                request.getOrganization().getFullName();
            }
            if (request.getMaterials() != null) {
                request.getMaterials().size();
            }
        });
        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByStatus(RequestStatus status) {
        List<Request> requests = requestRepository.findByStatus(status);
        // Forzar la inicialización de propiedades lazy
        requests.forEach(request -> {
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getFullName();
            }
            if (request.getOrganization() != null) {
                request.getOrganization().getUsername();
                request.getOrganization().getFullName();
            }
            if (request.getMaterials() != null) {
                request.getMaterials().size();
            }
        });
        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByOrganization(User organization) {
        List<Request> requests = requestRepository.findByOrganization(organization);
        // Forzar la inicialización de propiedades lazy
        requests.forEach(request -> {
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getFullName();
            }
            if (request.getOrganization() != null) {
                request.getOrganization().getUsername();
                request.getOrganization().getFullName();
            }
            if (request.getMaterials() != null) {
                request.getMaterials().size();
            }
        });
        return requests;
    }

    public Request approveRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus(RequestStatus.ACCEPTED);
            return requestRepository.save(request);
        }
        return null;
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public void deleteById(Long id) {
        requestRepository.deleteById(id);
    }

    public Request updateStatus(Long requestId, RequestStatus status) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus(status);
            return requestRepository.save(request);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Map<Long, Map<RequestStatus, Long>> getRequestStatsByUserIds(List<Long> userIds) {
        Map<Long, Map<RequestStatus, Long>> stats = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return stats;
        }

        List<Object[]> rows = requestRepository.countByUserIdsAndStatus(userIds);
        for (Object[] row : rows) {
            Long userId = (Long) row[0];
            RequestStatus status = (RequestStatus) row[1];
            Long count = (Long) row[2];

            stats.computeIfAbsent(userId, id -> new HashMap<>())
                 .put(status, count);
        }

        return stats;
    }

    // ====== Organization availability validation ======

    /**
     * Returns true if there is at least one organization available to handle requests
     * Note: Checks for active=true first, falls back to all organizations if none are active
     */
    public boolean hasActiveOrganizations() {
        List<User> activeOrgs = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        if (activeOrgs != null && !activeOrgs.isEmpty()) {
            return true;
        }
        // Fallback: check if there are any organizations at all
        List<User> allOrgs = userRepository.findByRole(Role.ORGANIZATION);
        return allOrgs != null && !allOrgs.isEmpty();
    }

    /**
     * Returns the list of organizations (prioritizes active=true, falls back to all)
     */
    @Transactional(readOnly = true)
    public List<User> getActiveOrganizations() {
        List<User> orgs = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        // Si no hay organizaciones activas, mostrar todas las organizaciones
        if (orgs == null || orgs.isEmpty()) {
            orgs = userRepository.findByRole(Role.ORGANIZATION);
        }
        // Forzar la inicialización de propiedades lazy
        orgs.forEach(org -> {
            org.getUsername();
            org.getFirstName();
            org.getLastName();
            org.getEmail();
        });
        return orgs;
    }

    /**
     * Convenience method to expose just organization names for the UI
     */
    public List<String> getActiveOrganizationNames() {
        return getActiveOrganizations().stream()
                .map(u -> u.getUsername() != null ? u.getUsername() : (u.getFullName() != null ? u.getFullName() : "Organización"))
                .collect(Collectors.toList());
    }
}
