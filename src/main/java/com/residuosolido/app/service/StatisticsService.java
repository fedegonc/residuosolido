package com.residuosolido.app.service;

import com.residuosolido.app.model.*;
import com.residuosolido.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for generating dashboard statistics and analytics
 */
@Service
public class StatisticsService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final MaterialRepository materialRepository;
    private final CategoryRepository categoryRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public StatisticsService(UserRepository userRepository,
                            RequestRepository requestRepository,
                            MaterialRepository materialRepository,
                            CategoryRepository categoryRepository,
                            FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.materialRepository = materialRepository;
        this.categoryRepository = categoryRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Get general dashboard statistics
     * @return Map containing various statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByRoleAndActive(Role.USER, true));
        stats.put("adminUsers", userRepository.countByRoleAndActive(Role.ADMIN, true));
        stats.put("organizationUsers", userRepository.countByRoleAndActive(Role.ORGANIZATION, true));

        // Request statistics
        stats.put("totalRequests", requestRepository.count());
        stats.put("pendingRequests", requestRepository.findByStatus(RequestStatus.PENDING).size());
        stats.put("completedRequests", requestRepository.findByStatus(RequestStatus.COMPLETED).size());
        stats.put("acceptedRequests", requestRepository.findByStatus(RequestStatus.ACCEPTED).size());
        stats.put("rejectedRequests", requestRepository.findByStatus(RequestStatus.REJECTED).size());

        // Material statistics
        stats.put("totalMaterials", materialRepository.count());
        stats.put("activeMaterials", materialRepository.findByActiveTrue().size());

        // Other statistics
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalFeedback", feedbackRepository.count());

        return stats;
    }

    /**
     * Get user registrations grouped by month
     * @return List of maps with year, month, and count
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUsersByMonth() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> queryResult = userRepository.findUsersByMonth();

        for (Object[] row : queryResult) {
            Map<String, Object> data = new HashMap<>();
            data.put("year", row[0]);
            data.put("month", row[1]);
            data.put("count", row[2]);
            result.add(data);
        }

        return result;
    }

    /**
     * Get request counts grouped by status
     * @return List of maps with status and count
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRequestsByStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> queryResult = requestRepository.countRequestsByStatus();

        for (Object[] row : queryResult) {
            Map<String, Object> data = new HashMap<>();
            RequestStatus status = (RequestStatus) row[0];
            data.put("status", status.name());
            data.put("displayName", status.getDisplayName());
            data.put("count", row[1]);
            result.add(data);
        }

        return result;
    }

    /**
     * Get material counts grouped by category
     * @return List of maps with category and count
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMaterialsByType() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Object[]> queryResult = materialRepository.countMaterialsByCategory();

        for (Object[] row : queryResult) {
            Map<String, Object> data = new HashMap<>();
            data.put("category", row[0]);
            data.put("count", row[1]);
            result.add(data);
        }

        return result;
    }
}
