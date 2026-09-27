package com.residuosolido.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita @Async — usado solo por NotificationEventListener por ahora, para
 * que una notificación lenta (o que falla) no bloquee el camino crítico de
 * aceptar/rechazar una solicitud (ver docs/TRADEOFFS.md §36). Pool chico y
 * acotado a propósito: el volumen de notificaciones es 1 por transición de
 * estado, no hace falta más que esto para el tamaño actual del sistema.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }
}
