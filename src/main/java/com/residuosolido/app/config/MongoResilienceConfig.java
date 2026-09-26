package com.residuosolido.app.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Timeouts explícitos para la conexión a MongoDB Atlas — sin esto, el driver
 * usa sus defaults (serverSelectionTimeout de 30s), así que si Atlas no
 * responde, cada request que toca la base queda colgado 30s antes de fallar
 * en vez de devolver un error rápido. Con esto, falla en ~5s.
 *
 * retryWrites/retryReads se fijan explícitos (aunque el driver moderno ya los
 * trae en true por default) para no depender de un comportamiento implícito:
 * cubre el caso real de un failover de primary en Atlas durante mantenimiento,
 * reintentando la operación una vez en el nuevo primary en vez de fallar.
 *
 * No agrega un circuit breaker ni reintentos con backoff: para una sola
 * instancia con Atlas como único backend, fail-fast + mensaje claro al
 * usuario (ver GlobalExceptionHandler#handleMongoException) es proporcional
 * al riesgo real. Ver docs/TRADEOFFS.md.
 */
@Configuration
public class MongoResilienceConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoTimeoutCustomizer() {
        return builder -> builder
                .applyToClusterSettings(cluster ->
                        cluster.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS))
                .applyToConnectionPoolSettings(pool ->
                        pool.maxWaitTime(5, TimeUnit.SECONDS))
                .retryWrites(true)
                .retryReads(true);
    }
}
