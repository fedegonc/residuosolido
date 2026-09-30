package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.YearMonth;
import java.util.Map;

@Data
@AllArgsConstructor
public class MonthlyReport {
    private YearMonth period;
    private Organization organization;
    private int totalRequests;
    private Map<RequestStatus, Integer> countByStatus;
    private Map<City, Integer> countByCity;
    private Map<MaterialCategory, Integer> countByMaterial;
    private MonthlyStats previousMonth;

    @Data
    @AllArgsConstructor
    public static class MonthlyStats {
        private int total;
        private int completed;
        private double completionRate;
    }

    public int getCompletedCount() {
        return countByStatus.getOrDefault(RequestStatus.COMPLETED, 0);
    }

    public int getPendingCount() {
        return countByStatus.getOrDefault(RequestStatus.PENDING, 0);
    }

    public int getRejectedCount() {
        return countByStatus.getOrDefault(RequestStatus.REJECTED, 0);
    }

    public int getInProgressCount() {
        return countByStatus.getOrDefault(RequestStatus.IN_PROGRESS, 0);
    }

    public double getCompletionPercentage() {
        return totalRequests > 0 ? (getCompletedCount() * 100.0) / totalRequests : 0;
    }

    public double getGrowthPercentage() {
        if (previousMonth == null || previousMonth.getTotal() == 0) return 0;
        return ((totalRequests - previousMonth.getTotal()) * 100.0) / previousMonth.getTotal();
    }
}
