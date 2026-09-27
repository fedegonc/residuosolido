package com.residuosolido.app.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita @Cacheable — usado solo por CityOrgService.getOrganizationsByCity
 * por ahora, para no recorrer/filtrar el listado completo de organizaciones
 * en cada carga del formulario de solicitud (ver docs/TRADEOFFS.md §37, el
 * trigger que motiva esto: p95 > 200ms sostenido o > 1000 req/día en
 * /solicitudes/org-options).
 *
 * Sin spring-boot-starter-cache: no hace falta, Spring Boot detecta
 * @EnableCaching y cae al ConcurrentMapCacheManager por defecto (in-memory,
 * sin dependencia nueva) — suficiente para un cache de 2 entradas (una por
 * ciudad, RIVERA/LIVRAMENTO) en una sola instancia.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
