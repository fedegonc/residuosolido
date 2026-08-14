package com.residuosolido.app.service;

import java.util.List;
import java.util.Map;

public final class MongoAggregationUtils {

    private MongoAggregationUtils() {}

    @SuppressWarnings("unchecked")
    public static long extractCount(Map result, String facetName) {
        Object facet = result.get(facetName);
        if (facet instanceof List<?> list && !list.isEmpty()) {
            Object count = ((Map<String, Object>) list.get(0)).get("count");
            return count instanceof Number ? ((Number) count).longValue() : 0L;
        }
        return 0L;
    }

    public static long extractSimpleCount(Map result, String fieldName) {
        Object value = result.get(fieldName);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
