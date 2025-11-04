package com.residuosolido.app.util;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Interceptor para contar queries ejecutadas en Hibernate
 */
public class QueryCounterInterceptor implements StatementInspector {
    private static final ThreadLocal<Integer> queryCount = ThreadLocal.withInitial(() -> 0);

    @Override
    public String inspect(String sql) {
        queryCount.set(queryCount.get() + 1);
        return sql;
    }

    public static int getQueryCount() {
        return queryCount.get();
    }

    public static void resetQueryCount() {
        queryCount.set(0);
    }

    public static void startCounting() {
        resetQueryCount();
    }

    public static void printQueryCount(String label) {
        System.out.println("📊 [" + label + "] Total queries ejecutadas: " + getQueryCount());
    }
}
