package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    public Request createRequest(User user, String description, String materials) {
        Request request = new Request();
        request.setUser(user);
        request.setDescription(description);
        request.setMaterials(materials);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        return requestRepository.save(request);
    }

    public Request createRequest(User user, String description, String materials, String address) {
        Request request = new Request();
        request.setUser(user);
        request.setDescription(description);
        request.setMaterials(materials);
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

    public List<Request> getRequestsByUser(User user) {
        return requestRepository.findByUser(user);
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
    
    public List<Request> getPendingRequests() {
        return requestRepository.findByStatus(RequestStatus.PENDING);
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
     * Returns true if there is at least one active organization available to handle requests
     */
    public boolean hasActiveOrganizations() {
        List<User> orgs = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        return orgs != null && !orgs.isEmpty();
    }

    /**
     * Returns the list of active organizations (users with role ORGANIZATION and active=true)
     */
    @Transactional(readOnly = true)
    public List<User> getActiveOrganizations() {
        List<User> orgs = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
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
