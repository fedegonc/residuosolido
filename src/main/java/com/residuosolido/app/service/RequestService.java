package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.enums.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.EnumMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public Request createRequest(User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, String guestName, String guestPhone) {
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
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        User org = assignOrganization(city);
        request.assignOrganization(org);
        logger.info("Organización auto-asignada: {} para ciudad: {}", org.getDisplayName(), city);

        return requestRepository.save(request);
    }

    public List<Request> getRequestsByUser(User user) {
        return requestRepository.findByUser(user);
    }

    public List<Request> getRecentRequestsByUser(User user, int limit) {
        return requestRepository.findByUser(user).stream().limit(limit).toList();
    }

    public Request getOwnedRequest(String id, User user) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.request.not_found"));
        if (request.getUser() == null || !request.getUser().getId().equals(user.getId())) {
            throw new SecurityException("flash.request.not_owned");
        }
        return request;
    }

    public Request getEditableOwnedRequest(String id, User user) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
        return request;
    }

    public void deleteOwnedRequest(String id, User user) {
        getOwnedRequest(id, user);
        requestRepository.deleteById(id);
    }

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = getOwnedOrgRequest(id, org);
        request.accept(slot);
        requestRepository.save(request);
    }

    public void rejectRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.reject();
        requestRepository.save(request);
    }

    public void completeRequest(String id, User org) {
        Request request = getOwnedOrgRequest(id, org);
        request.complete();
        requestRepository.save(request);
    }

    public Request getOwnedOrgRequest(String id, User org) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("flash.org.request_not_found"));
        if (request.getOrganization() == null || !request.getOrganization().getId().equals(org.getId())) {
            throw new SecurityException("flash.org.request_not_owned");
        }
        return request;
    }

    public Request updateRequest(String id, User user, City city, String address, String addressReference,
                                  List<MaterialCategory> materials, MultipartFile imageFile,
                                  LocalImageService imageService) {
        Request request = getOwnedRequest(id, user);
        if (!request.canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
        request.setCity(city);
        request.setAddress(address);
        request.setAddressReference(addressReference);
        request.setMaterials(materials != null ? materials : List.of());
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                request.setImageUrl(imageService.uploadFile(imageFile));
            } catch (Exception e) {
                logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
            }
        }
        return requestRepository.save(request);
    }

    public List<Request> getRequestsByOrganizationAndStatus(User organization, RequestStatus status) {
        return getRequestsByOrganization(organization).stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Request> getRecentPendingRequestsByOrganization(User organization, int limit) {
        return getRequestsByOrganization(organization).stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .limit(limit)
                .toList();
    }

    public List<Request> getGuestRequestsByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        return requestRepository.findByGuestPhoneOrderByCreatedAtDesc(phone.trim());
    }

    public List<Request> getOrgRequestsByStatusFilter(User organization, String status) {
        if (status == null || status.trim().isEmpty()) {
            return getRequestsByOrganization(organization);
        }
        try {
            RequestStatus filterStatus = RequestStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
            return getRequestsByOrganizationAndStatus(organization, filterStatus);
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de estado inválido: {}", status);
            return getRequestsByOrganization(organization);
        }
    }

    public Map<String, Long> getOrgDashboardData(User organization) {
        Map<RequestStatus, Long> pendingGrouped = countGroupedByOrganizationAndStatuses(
            organization, List.of(RequestStatus.PENDING));
        long pendingCount = pendingGrouped.getOrDefault(RequestStatus.PENDING, 0L);

        Map<RequestStatus, Long> groupedCounts = countGroupedByOrganizationAndStatuses(
            organization, List.of(RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED));
        long inProgressCount = groupedCounts.getOrDefault(RequestStatus.IN_PROGRESS, 0L);
        long completedCount = groupedCounts.getOrDefault(RequestStatus.COMPLETED, 0L);

        Map<String, Long> data = new java.util.HashMap<>();
        data.put("pending", pendingCount);
        data.put("inProgress", inProgressCount);
        data.put("completed", completedCount);
        return data;
    }

    public Optional<Request> findById(String id) {
        return requestRepository.findById(id);
    }

    public List<Request> getRequestsByOrganization(User organization) {
        return requestRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }

    private User assignOrganization(City city) {
        if (city == null) {
            throw new IllegalArgumentException("request.error.city_required");
        }
        List<User> orgs = userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, city, true);
        if (orgs == null || orgs.isEmpty()) {
            orgs = userRepository.findByRoleAndCity(Role.ORGANIZATION, city);
        }
        if (orgs == null || orgs.isEmpty()) {
            throw new IllegalStateException("request.error.no_org_in_city");
        }
        return orgs.get(0);
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public Request createRequestWithImage(User user, City city, String address, String addressReference,
                                            List<MaterialCategory> materials, String guestName, String guestPhone,
                                            MultipartFile imageFile, LocalImageService imageService) {
        Request request = createRequest(user, city, address, addressReference, materials, guestName, guestPhone);
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = imageService.uploadFile(imageFile);
                request.setImageUrl(imageUrl);
                request = requestRepository.save(request);
            } catch (Exception e) {
                logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
            }
        }
        return request;
    }

    public void deleteById(String id) {
        requestRepository.deleteById(id);
    }

    public java.util.Map<String, Long> getDashboardStats(User user) {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        List<Request> requests = getRequestsByUser(user);
        stats.put("total", (long) requests.size());
        stats.put("pending", requests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count());
        stats.put("inProgress", requests.stream().filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count());
        stats.put("completed", requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count());
        return stats;
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

    public List<User> getOrganizationsByCity(City city) {
        List<User> orgs = userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, city, true);
        if (orgs == null || orgs.isEmpty()) {
            orgs = userRepository.findByRoleAndCity(Role.ORGANIZATION, city);
        }
        return orgs;
    }

    public List<City> getAvailableCities() {
        return List.of(City.values()).stream()
            .filter(c -> {
                List<User> orgs = getOrganizationsByCity(c);
                return orgs != null && !orgs.isEmpty();
            })
            .toList();
    }

    public long getPublicTotalCompleted() {
        return getPublicMetricsByCity().values().stream().mapToLong(Long::longValue).sum();
    }

    public Map<String, Long> getPublicMetricsByCity() {
        List<Request> completed = requestRepository.findByStatus(RequestStatus.COMPLETED);
        return completed.stream()
                .filter(r -> r.getCity() != null)
                .collect(Collectors.groupingBy(r -> r.getCity().name(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new));
    }
}
