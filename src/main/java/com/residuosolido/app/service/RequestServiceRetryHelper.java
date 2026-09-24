package com.residuosolido.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.function.Supplier;

/**
 * Helper para reintentos con backoff exponencial ante conflictos OptimisticLocking.
 * Si la operación falla por concurrencia, espera y reintenta hasta MAX_RETRIES veces.
 *
 * Patrón: `retryOnOptimisticLockFailure(() -> operation(), "OperationName")`
 * Si todos los reintentos fallan, lanza la última excepción.
 */
public final class RequestServiceRetryHelper {

    private static final Logger logger = LoggerFactory.getLogger(RequestServiceRetryHelper.class);
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_WAIT_MS = 100;

    private RequestServiceRetryHelper() {}

    /**
     * Ejecuta la operación con reintentos automáticos ante OptimisticLockingFailureException.
     *
     * @param operation Supplier que retorna el resultado de la operación
     * @param operationName Nombre de la operación para logging
     * @param <T> Tipo del resultado
     * @return El resultado de la operación si tiene éxito
     * @throws OptimisticLockingFailureException si todos los reintentos fallan
     */
    public static <T> T retryOnOptimisticLockFailure(Supplier<T> operation, String operationName) {
        int attempt = 0;
        OptimisticLockingFailureException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (OptimisticLockingFailureException e) {
                lastException = e;
                attempt++;

                if (attempt >= MAX_RETRIES) {
                    logger.error("OPTIMISTIC_LOCK_EXHAUSTED: {}, agotados {} intentos", operationName, MAX_RETRIES);
                    throw e;
                }

                long waitMs = INITIAL_WAIT_MS * (long) Math.pow(2, attempt - 1);
                logger.warn("OPTIMISTIC_LOCK_RETRY: operation={}, attempt={}/{}, waitMs={}",
                    operationName, attempt, MAX_RETRIES, waitMs);

                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("OPTIMISTIC_LOCK_INTERRUPTED: {}, attempt={}", operationName, attempt);
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }

        assert lastException != null;
        throw lastException;
    }
}
