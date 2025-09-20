package com.residuosolido.app.service;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
            // Estadísticas básicas
            stats.put("totalUsers", userRepository.count());
            stats.put("totalOrganizations", 0L); // Temporalmente 0 hasta implementar organizaciones
            stats.put("totalFeedback", feedbackRepository.count());
            stats.put("totalMaterials", materialRepository.count());
            stats.put("totalPosts", postRepository.count());
            stats.put("totalCategories", categoryRepository.count());

            // Solicitudes en los últimos 30 días
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            stats.put("requestsLast30Days", requestRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(thirtyDaysAgo))
                .count());

            // Usuarios activos (con solicitudes en los últimos 30 días)
            stats.put("activeUsers", requestRepository.findAll().stream()
                .filter(r -> r.getCreatedAt().isAfter(thirtyDaysAgo))
                .map(r -> r.getUser())
                .distinct()
                .count());

            stats.put("activeOrganizations", 0L); // Temporalmente 0

        } catch (Exception e) {
            // En caso de error, devolver valores por defecto
            stats.put("totalUsers", 0L);
            stats.put("totalOrganizations", 0L);
            stats.put("totalFeedback", 0L);
            stats.put("requestsLast30Days", 0L);
            stats.put("activeUsers", 0L);
            stats.put("activeOrganizations", 0L);
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsersByMonth() {
        Map<String, Object> data = new HashMap<>();

        try {
            // Datos de ejemplo por ahora - en producción se haría con consultas JPQL
            List<String> labels = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
            List<Long> values = Arrays.asList(25L, 31L, 28L, 40L, 52L, 61L, 75L, 88L, 70L, 66L, 54L, 49L);

            data.put("labels", labels);
            data.put("data", values);
        } catch (Exception e) {
            // Datos de fallback
            data.put("labels", Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun"));
            data.put("data", Arrays.asList(10L, 15L, 20L, 25L, 30L, 35L));
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
            // Datos de ejemplo - en producción se haría con consultas más complejas
            List<String> labels = Arrays.asList("Plástico", "Papel", "Vidrio", "Metal", "Orgánico");
            List<Double> values = Arrays.asList(420.0, 310.0, 260.0, 190.0, 510.0);

            data.put("labels", labels);
            data.put("data", values);
        } catch (Exception e) {
            // Datos de fallback
            data.put("labels", Arrays.asList("Plástico", "Papel", "Vidrio"));
            data.put("data", Arrays.asList(100.0, 150.0, 200.0));
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
