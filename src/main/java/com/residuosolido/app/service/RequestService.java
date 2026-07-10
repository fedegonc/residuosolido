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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.EnumMap;
import java.util.ArrayList;

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
                    String materialId = idStr.trim();
                    materialRepository.findById(materialId).ifPresent(materialList::add);
                } catch (Exception e) {
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

    public List<Request> getRequestsByUser(User user) {
        return requestRepository.findByUser(user);
    }

    public Optional<Request> findById(String id) {
        return requestRepository.findById(id);
    }

    public List<Request> getRequestsByOrganization(User organization) {
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }

    public List<Request> getRequestsByStatusesAndOrganization(List<RequestStatus> statuses, User organization) {
        return requestRepository.findByStatusInAndOrganizationOrderByCreatedAtDesc(statuses, organization);
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public void deleteById(String id) {
        requestRepository.deleteById(id);
    }

    public long countByStatusesAndOrganization(List<RequestStatus> statuses, User organization) {
        if (organization == null || statuses == null || statuses.isEmpty()) {
            return 0L;
        }
        return requestRepository.countByStatusInAndOrganization(statuses, organization);
    }

    public Map<RequestStatus, Long> countGroupedByOrganizationAndStatuses(User organization, List<RequestStatus> statuses) {
        Map<RequestStatus, Long> map = new EnumMap<>(RequestStatus.class);
        if (organization == null || statuses == null || statuses.isEmpty()) {
            return map;
        }
        List<Request> requests = requestRepository.findByStatusInAndOrganizationOrderByCreatedAtDesc(statuses, organization);
        for (Request request : requests) {
            map.merge(request.getStatus(), 1L, Long::sum);
        }
        return map;
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
    public List<User> getActiveOrganizations() {
        List<User> orgs = userRepository.findByRoleAndActive(Role.ORGANIZATION, true);
        if (orgs == null || orgs.isEmpty()) {
            orgs = userRepository.findByRole(Role.ORGANIZATION);
        }
        return orgs;
    }

}
