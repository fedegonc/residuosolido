package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequestMetricsService {

    private final RequestRepository requestRepository;

    @Autowired
    public RequestMetricsService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public Map<String, Long> getUserDashboardStats(User user) {
        Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", requestRepository.countByUser(user));
        stats.put("pending", requestRepository.countByUserAndStatus(user, RequestStatus.PENDING));
        stats.put("inProgress", requestRepository.countByUserAndStatus(user, RequestStatus.IN_PROGRESS));
        stats.put("completed", requestRepository.countByUserAndStatus(user, RequestStatus.COMPLETED));
        return stats;
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
