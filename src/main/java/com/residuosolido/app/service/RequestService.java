package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.Material;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

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
        
        request.setCollectionAddress(address);
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
                try {
                    // Forzar carga de propiedades básicas del proxy
                    request.getOrganization().getId();
                    request.getOrganization().getUsername();
                } catch (Exception e) {
                    logger.warn("Error al inicializar organización para request {}: {}", request.getId(), e.getMessage());
                }
            }
        });
        return requests;
    }

    @Transactional(readOnly = true)
    public Page<Request> searchAll(String query, Pageable pageable) {
        Page<Request> page = requestRepository.searchAll(query, pageable);
        return page;
    }

    public long count() {
        return requestRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Request> findAll() {
        List<Request> requests = requestRepository.findAll();
        requests.forEach(request -> {
            if (request.getUser() != null) {
                request.getUser().getUsername();
                request.getUser().getFirstName();
                request.getUser().getLastName();
                request.getUser().getEmail();
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
    public Optional<Request> findById(Long id) {
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Ejecutando query findByIdWithDetails para ID: " + id);
        
        // Usar método optimizado que carga todas las relaciones en una sola consulta SQL
        Optional<Request> result = requestRepository.findByIdWithDetails(id);
        
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ Query completada en: " + elapsed + "ms");
        
        return result;
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
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Cargando solicitudes pendientes");

        List<Request> requests = requestRepository.findByStatusWithDetails(RequestStatus.PENDING);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ " + requests.size() + " solicitudes pendientes cargadas en: " + elapsed + "ms");

        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByStatus(RequestStatus status) {
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Cargando solicitudes con estado: " + status);

        List<Request> requests = requestRepository.findByStatusWithDetails(status);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ " + requests.size() + " solicitudes cargadas en: " + elapsed + "ms");

        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByOrganization(User organization) {
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Cargando solicitudes de organización: " + organization.getUsername());

        List<Request> requests = requestRepository.findByOrganizationWithDetails(organization);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ " + requests.size() + " solicitudes cargadas en: " + elapsed + "ms");

        return requests;
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByStatusesAndOrganization(List<RequestStatus> statuses, User organization) {
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Cargando solicitudes por estados " + statuses + " para organización: " + organization.getUsername());

        List<Request> requests = requestRepository.findByStatusesAndOrganizationWithDetails(statuses, organization);

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ " + requests.size() + " solicitudes cargadas en: " + elapsed + "ms");

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

    @Transactional(readOnly = true)
    public long countRequestsForMaterials(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return 0L;
        }
        return requestRepository.countDistinctByMaterials(materialIds);
    }

    @Transactional(readOnly = true)
    public long countRequestsByStatusForMaterials(List<Long> materialIds, RequestStatus status) {
        if (materialIds == null || materialIds.isEmpty()) {
            return 0L;
        }
        return requestRepository.countDistinctByMaterialsAndStatus(materialIds, status);
    }

    @Transactional(readOnly = true)
    public Map<RequestStatus, Long> getRequestStatusCountsForMaterials(List<Long> materialIds) {
        Map<RequestStatus, Long> statusCounts = new EnumMap<>(RequestStatus.class);
        if (materialIds == null || materialIds.isEmpty()) {
            return statusCounts;
        }
        List<Object[]> rows = requestRepository.countByStatusForMaterials(materialIds);
        for (Object[] row : rows) {
            RequestStatus status = (RequestStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status, count);
        }
        return statusCounts;
    }

    @Transactional(readOnly = true)
    public long countByStatus(RequestStatus status) {
        return requestRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByStatusAndMaterials(RequestStatus status, List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return countByStatus(status);
        }
        // Incluir solicitudes sin materiales asignados
        return requestRepository.countByStatusWithOptionalMaterials(status, materialIds, true);
    }

    @Transactional(readOnly = true)
    public long countByStatusesAndOrganization(List<RequestStatus> statuses, User organization) {
        if (organization == null || statuses == null || statuses.isEmpty()) {
            return 0L;
        }
        // Usar query nativa para contar sin traer todas las solicitudes
        return requestRepository.countByStatusesAndOrganization(statuses, organization);
    }

    @Transactional(readOnly = true)
    public Map<RequestStatus, Long> countGroupedByOrganizationAndStatuses(User organization, List<RequestStatus> statuses) {
        Map<RequestStatus, Long> map = new EnumMap<>(RequestStatus.class);
        if (organization == null || statuses == null || statuses.isEmpty()) {
            return map;
        }
        List<Object[]> rows = requestRepository.countGroupedByOrganizationAndStatuses(organization, statuses);
        for (Object[] row : rows) {
            RequestStatus status = (RequestStatus) row[0];
            Long count = (Long) row[1];
            map.put(status, count);
        }
        return map;
    }

    @Transactional(readOnly = true)
    public void logPendingRequestsDebug() {
        List<Request> pending = requestRepository.findByStatusWithDetails(RequestStatus.PENDING);
        System.out.println("🔎 DEBUG Pending total con detalles: " + pending.size());
        for (Request request : pending) {
            List<Long> materialIds = request.getMaterials() != null
                    ? request.getMaterials().stream().map(material -> material.getId()).toList()
                    : java.util.Collections.emptyList();
            System.out.println("    • Request ID " + request.getId() +
                    " | Status=" + request.getStatus() +
                    " | Org=" + (request.getOrganization() != null ? request.getOrganization().getId() : "-") +
                    " | Materiales=" + materialIds);
        }
    }

    /**
     * Obtiene top N solicitudes pendientes filtradas por materiales (optimizado para dashboard)
     * Usa estrategia de 2 queries: primero IDs con LIMIT en BD, luego entidades completas
     */
    @Transactional(readOnly = true)
    public List<Request> getTopPendingByMaterials(List<Long> materialIds, int limit) {
        if (materialIds == null || materialIds.isEmpty()) {
            System.out.println("  ⚠️ Lista de materiales vacía, retornando 0 solicitudes");
            return new ArrayList<>();
        }
        long start = System.currentTimeMillis();
        System.out.println("  🔍 Cargando top " + limit + " solicitudes pendientes filtradas por materiales: " + materialIds);
        
        // Paso 1: Traer solo IDs con LIMIT aplicado en BD (query nativa rápida)
        System.out.println("  🔍 Ejecutando query nativa para IDs con status: " + RequestStatus.PENDING.name());
        List<Object[]> results = requestRepository.findTopIdsByStatusAndMaterials(
            RequestStatus.PENDING.name(), 
            materialIds, 
            limit
        );
        System.out.println("  📊 Query de IDs retornó: " + results.size() + " filas");
        
        // Extraer solo los IDs (primera columna)
        List<Long> ids = results.stream()
            .map(row -> {
                System.out.println("    - ID: " + row[0] + ", created_at: " + row[1]);
                return ((Number) row[0]).longValue();
            })
            .collect(Collectors.toList());
        System.out.println("  📋 IDs extraídos: " + ids);
        
        // Paso 2: Si hay IDs, traer entidades completas con eager loading
        List<Request> requests = new ArrayList<>();
        if (!ids.isEmpty()) {
            System.out.println("  🔍 Cargando entidades completas para IDs: " + ids);
            requests = requestRepository.findByIdsWithDetails(ids);
            System.out.println("  ✅ Entidades cargadas: " + requests.size());
        } else {
            System.out.println("  ⚠️ No se encontraron IDs, retornando lista vacía");
        }
        
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  ✅ " + requests.size() + " solicitudes cargadas en: " + elapsed + "ms (2 queries optimizadas)");
        return requests;
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
