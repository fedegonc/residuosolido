package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.MonthlyReport;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonthlyReportService {

    private final RequestRepository requestRepository;

    public MonthlyReportService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public MonthlyReport generateForMonth(Organization org, YearMonth month) {
        List<Request> requests = getRequestsForMonth(org, month);
        Map<RequestStatus, Integer> countByStatus = countByStatus(requests);
        Map<City, Integer> countByCity = countByCity(requests);
        Map<MaterialCategory, Integer> countByMaterial = countByMaterial(requests);

        int totalCompleted = countByStatus.getOrDefault(RequestStatus.COMPLETED, 0);
        double completionRate = requests.isEmpty() ? 0 : (totalCompleted * 100.0) / requests.size();

        MonthlyReport.MonthlyStats currentStats = new MonthlyReport.MonthlyStats(
                requests.size(),
                totalCompleted,
                completionRate
        );

        MonthlyReport.MonthlyStats previousStats = null;
        if (!month.equals(YearMonth.now())) {
            previousStats = getStatsForMonth(org, month.minusMonths(1));
        }

        return new MonthlyReport(
                month,
                org,
                requests.size(),
                countByStatus,
                countByCity,
                countByMaterial,
                previousStats
        );
    }

    private List<Request> getRequestsForMonth(Organization org, YearMonth month) {
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);

        return requestRepository.findByOrganizationOrderByCreatedAtDesc(org, PageRequest.of(0, 10000))
                .stream()
                .filter(r -> r.getCreatedAt() != null &&
                           r.getCreatedAt().isAfter(startOfMonth.minusDays(1)) &&
                           r.getCreatedAt().isBefore(endOfMonth.plusDays(1)))
                .collect(Collectors.toList());
    }

    private Map<RequestStatus, Integer> countByStatus(List<Request> requests) {
        Map<RequestStatus, Integer> counts = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            counts.put(status, 0);
        }
        requests.forEach(r -> counts.merge(r.getStatus(), 1, Integer::sum));
        return counts;
    }

    private Map<City, Integer> countByCity(List<Request> requests) {
        Map<City, Integer> counts = new EnumMap<>(City.class);
        requests.stream()
                .filter(r -> r.getCity() != null)
                .forEach(r -> counts.merge(r.getCity(), 1, Integer::sum));
        return counts;
    }

    private Map<MaterialCategory, Integer> countByMaterial(List<Request> requests) {
        Map<MaterialCategory, Integer> counts = new EnumMap<>(MaterialCategory.class);
        requests.stream()
                .flatMap(r -> r.getMaterials().stream())
                .forEach(m -> counts.merge(m, 1, Integer::sum));
        return counts;
    }

    private MonthlyReport.MonthlyStats getStatsForMonth(Organization org, YearMonth month) {
        List<Request> requests = getRequestsForMonth(org, month);
        int completed = (int) requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
        double rate = requests.isEmpty() ? 0 : (completed * 100.0) / requests.size();
        return new MonthlyReport.MonthlyStats(requests.size(), completed, rate);
    }
}
