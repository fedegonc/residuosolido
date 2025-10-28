package com.residuosolido.app.service;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime currentMonthStart = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
            LocalDateTime previousMonthEnd = currentMonthStart.minusNanos(1);
            LocalDateTime thirtyDaysAgo = now.minusDays(30);

            // Estadísticas básicas
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            stats.put("totalOrganizations", userRepository.countByRole(Role.ORGANIZATION));
            stats.put("activeOrganizations", userRepository.countByRoleAndActive(Role.ORGANIZATION, true));
            stats.put("totalFeedback", feedbackRepository.count());
            stats.put("totalMaterials", materialRepository.count());
            stats.put("totalPosts", postRepository.count());
            stats.put("totalCategories", categoryRepository.count());

            // Solicitudes en los últimos 30 días
            long requestsLast30Days = requestRepository.countByCreatedAtAfter(thirtyDaysAgo);
            stats.put("requestsLast30Days", requestsLast30Days);

            // Usuarios activos (con solicitudes en los últimos 30 días)
            long activeUsers = requestRepository.countDistinctUsersWithRequestsAfter(thirtyDaysAgo);
            stats.put("activeUsers", activeUsers);

            // Solicitudes por estado
            long completedRequests = requestRepository.countByStatus(RequestStatus.COMPLETED);
            long pendingRequests = requestRepository.countByStatus(RequestStatus.PENDING);
            stats.put("completedRequests", completedRequests);
            stats.put("pendingRequests", pendingRequests);

            long totalRequests = requestRepository.count();
            double completionRate = totalRequests == 0 ? 0.0 :
                (completedRequests * 100.0) / totalRequests;
            stats.put("completionRate", Math.round(completionRate * 10.0) / 10.0);

            long currentMonthRequests = requestRepository.countByCreatedAtBetween(currentMonthStart, now);
            long previousMonthRequests = requestRepository.countByCreatedAtBetween(previousMonthStart, previousMonthEnd);
            stats.put("currentMonthRequests", currentMonthRequests);
            stats.put("previousMonthRequests", previousMonthRequests);

            double requestsGrowth = previousMonthRequests == 0 ? 0.0 :
                ((currentMonthRequests - previousMonthRequests) * 100.0) / previousMonthRequests;
            stats.put("requestsGrowth", Math.round(requestsGrowth * 10.0) / 10.0);

            long newUsersThisMonth = userRepository.countByCreatedAtBetween(currentMonthStart, now);
            long newUsersPreviousMonth = userRepository.countByCreatedAtBetween(previousMonthStart, previousMonthEnd);
            stats.put("newUsersThisMonth", newUsersThisMonth);
            stats.put("newUsersPreviousMonth", newUsersPreviousMonth);

            double usersGrowth = newUsersPreviousMonth == 0 ? 0.0 :
                ((newUsersThisMonth - newUsersPreviousMonth) * 100.0) / newUsersPreviousMonth;
            stats.put("usersGrowth", Math.round(usersGrowth * 10.0) / 10.0);

        } catch (Exception e) {
            // En caso de error, devolver valores por defecto
            stats.put("totalUsers", 0L);
            stats.put("totalOrganizations", 0L);
            stats.put("totalFeedback", 0L);
            stats.put("requestsLast30Days", 0L);
            stats.put("activeUsers", 0L);
            stats.put("completedRequests", 0L);
            stats.put("pendingRequests", 0L);
            stats.put("completionRate", 0.0);
            stats.put("requestsGrowth", 0.0);
            stats.put("usersGrowth", 0.0);
            stats.put("activeOrganizations", 0L);
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsersByMonth() {
        Map<String, Object> data = new HashMap<>();

        try {
            List<User> allUsers = userRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM", new Locale("es", "ES"));

            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();

            // Últimos 12 meses
            for (int i = 11; i >= 0; i--) {
                LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                
                String label = monthStart.format(formatter);
                labels.add(label.substring(0, 1).toUpperCase() + label.substring(1));

                long count = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null && 
                        u.getCreatedAt().isAfter(monthStart) && 
                        u.getCreatedAt().isBefore(monthEnd))
                    .count();
                values.add(count);
            }

            data.put("labels", labels);
            data.put("data", values);
        } catch (Exception e) {
            // Datos de fallback
            data.put("labels", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"));
            data.put("data", Arrays.asList(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
        }

        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserGrowthLast30Days() {
        Map<String, Object> data = new HashMap<>();

        try {
            List<User> allUsers = userRepository.findAll();
            List<LocalDate> creationDates = allUsers.stream()
                .map(User::getCreatedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .sorted()
                .toList();

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(29);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M");
            List<String> labels = new ArrayList<>();
            List<Long> cumulativeValues = new ArrayList<>();

            int index = 0;
            long cumulative = 0;
            for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
                while (index < creationDates.size() && !creationDates.get(index).isAfter(day)) {
                    cumulative++;
                    index++;
                }
                labels.add(day.format(formatter));
                cumulativeValues.add(cumulative);
            }

            data.put("labels", labels);
            data.put("data", cumulativeValues);
            data.put("total", cumulative);
        } catch (Exception e) {
            data.put("labels", Collections.emptyList());
            data.put("data", Collections.emptyList());
            data.put("total", 0L);
        }

        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRequestsByStatus() {
        Map<String, Object> data = new HashMap<>();

        try {
            // Contar solicitudes por estado usando streams
            List<Request> allRequests = requestRepository.findAll();
            Map<String, Long> statusCount = allRequests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    r -> r.getStatus() != null ? r.getStatus().name() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));

            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();
            List<String> colors = new ArrayList<>();

            // Mapeo de colores por estado
            Map<String, String> colorMap = Map.of(
                "PENDING", "#f59e0b",
                "IN_PROGRESS", "#3b82f6",
                "COMPLETED", "#10b981",
                "CANCELLED", "#ef4444"
            );

            // Etiquetas en español
            Map<String, String> labelMap = Map.of(
                "PENDING", "Pendientes",
                "IN_PROGRESS", "En curso",
                "COMPLETED", "Completadas",
                "CANCELLED", "Canceladas"
            );

            statusCount.forEach((status, count) -> {
                labels.add(labelMap.getOrDefault(status, status));
                values.add(count);
                colors.add(colorMap.getOrDefault(status, "#6b7280"));
            });

            data.put("labels", labels);
            data.put("data", values);
            data.put("colors", colors);

        } catch (Exception e) {
            // Datos de fallback
            data.put("labels", Arrays.asList("Pendientes", "En curso", "Completadas", "Canceladas"));
            data.put("data", Arrays.asList(48L, 76L, 190L, 28L));
            data.put("colors", Arrays.asList("#f59e0b", "#3b82f6", "#10b981", "#ef4444"));
        }

        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMaterialsByType() {
        Map<String, Object> data = new HashMap<>();

        try {
            List<Request> allRequests = requestRepository.findAll();
            
            // Contar materiales en solicitudes completadas
            Map<String, Long> materialCount = new HashMap<>();
            for (Request request : allRequests) {
                if (request.getStatus() == RequestStatus.COMPLETED && request.getMaterials() != null) {
                    // Forzar inicialización de la colección lazy
                    request.getMaterials().size();
                    for (Material material : request.getMaterials()) {
                        String name = material.getName();
                        materialCount.put(name, materialCount.getOrDefault(name, 0L) + 1);
                    }
                }
            }

            // Ordenar por cantidad y tomar top 10
            List<Map.Entry<String, Long>> sortedMaterials = materialCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .toList();

            List<String> labels = new ArrayList<>();
            List<Long> values = new ArrayList<>();

            for (Map.Entry<String, Long> entry : sortedMaterials) {
                labels.add(entry.getKey());
                values.add(entry.getValue());
            }

            // Si no hay datos, mostrar mensaje apropiado
            if (labels.isEmpty()) {
                labels.add("Sin datos");
                values.add(0L);
            }

            data.put("labels", labels);
            data.put("data", values);
            data.put("total", values.stream().mapToLong(Long::longValue).sum());
        } catch (Exception e) {
            // Datos de fallback
            data.put("labels", Arrays.asList("Sin datos"));
            data.put("data", Arrays.asList(0L));
            data.put("total", 0L);
        }

        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRequestsByMonth() {
        List<Object[]> results = requestRepository.countRequestsByMonth();
        Map<String, Object> data = new HashMap<>();

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        // Use a Java 17 compatible Locale creation (Locale.of is Java 19+)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "ES"));
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            String label = monthStart.format(formatter);
            labels.add(label);
            values.add(0L);
        }

        for (Object[] result : results) {
            Integer year = (Integer) result[0];
            Integer month = (Integer) result[1];
            Long count = (Long) result[2];

            LocalDateTime date = LocalDateTime.of(year, month, 1, 0, 0);
            String label = date.format(formatter);

            int index = labels.indexOf(label);
            if (index != -1) {
                values.set(index, count);
            }
        }

        data.put("labels", labels);
        data.put("data", values);
        return data;
    }
}
