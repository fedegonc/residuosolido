package com.residuosolido.app.load;

import com.residuosolido.app.config.Routes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test de carga: no mide throughput real de producción (para eso hace
 * falta un entorno dedicado, no la máquina de dev/CI corriendo otras cosas),
 * mide algo más chico y honesto: ¿la app se cae o degrada catastróficamente
 * bajo concurrencia moderada, o responde de forma estable?
 *
 * Deliberadamente NO usa una herramienta externa (k6/JMeter/Gatling): para
 * el tamaño de este proyecto (0 usuarios reales, 1 instancia Render) agregar
 * una herramienta de carga nueva es más infraestructura de la que el riesgo
 * justifica. Un ExecutorService + TestRestTemplate corriendo dentro del
 * mismo `mvn test` alcanza para lo que hace falta demostrar acá — mismo
 * criterio que se usó para no agregar Resilience4j en MongoResilienceConfig
 * (ver docs/TRADEOFFS.md §34).
 *
 * Solo pega a GET idempotentes (home, form de solicitud, health) — no crea
 * datos de carga (evita ensuciar la base con solicitudes de humo).
 *
 * No hay un RN formal de "Nms p95" en docs/REQUISITOS.md contra el cual
 * validar (se descartó un draft de SRS con esa métrica en una sesión
 * anterior — ver memoria de proyecto). Este test reporta percentiles como
 * información, y solo falla la build si algo se rompe (error rate) o si la
 * degradación es evidente (>2s de p95), no contra un SLA que no existe.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("load")
class LoadSmokeTest {

    private static final Logger logger = LoggerFactory.getLogger(LoadSmokeTest.class);
    private static final int CONCURRENT_USERS = 20;
    private static final int REQUESTS_PER_USER = 15;
    private static final long DEGRADED_P95_MS = 2000;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    void warmup() {
        // Descarta el primer hit (carga de clases, JIT frío) para no ensuciar
        // las métricas con un outlier que no representa carga sostenida.
        restTemplate.getForEntity(url(Routes.HOME), String.class);
    }

    @Test
    void homePage_soportaConcurrenciaModerada() {
        runSmoke("home", Routes.HOME);
    }

    @Test
    void requestForm_soportaConcurrenciaModerada() {
        runSmoke("solicitar", Routes.REQUESTS_NEW);
    }

    @Test
    void healthEndpoint_soportaConcurrenciaModerada() {
        runSmoke("health", "/actuator/health");
    }

    private void runSmoke(String label, String path) {
        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_USERS);
        try {
            List<Callable<Long>> tasks = IntStream
                    .range(0, CONCURRENT_USERS * REQUESTS_PER_USER)
                    .<Callable<Long>>mapToObj(i -> () -> timedGet(path))
                    .collect(Collectors.toList());

            List<Future<Long>> futures = pool.invokeAll(tasks, 60, TimeUnit.SECONDS);
            List<Long> latencies = futures.stream()
                    .map(this::safeGet)
                    .collect(Collectors.toList());

            long errors = latencies.stream().filter(l -> l < 0).count();
            List<Long> ok = latencies.stream().filter(l -> l >= 0).sorted().collect(Collectors.toList());

            long p50 = percentile(ok, 50);
            long p95 = percentile(ok, 95);
            long max = ok.isEmpty() ? -1 : ok.get(ok.size() - 1);

            logger.info("LOAD_SMOKE [{}]: requests={}, errors={}, p50={}ms, p95={}ms, max={}ms",
                    label, latencies.size(), errors, p50, p95, max);

            assertTrue(errors == 0,
                    label + ": " + errors + " requests fallaron de " + latencies.size());
            assertTrue(p95 < DEGRADED_P95_MS,
                    label + ": p95=" + p95 + "ms supera el umbral de degradación (" + DEGRADED_P95_MS + "ms)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }
    }

    private long timedGet(String path) {
        long start = System.nanoTime();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url(path), String.class);
            if (!response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.FOUND) {
                return -1;
            }
            return (System.nanoTime() - start) / 1_000_000;
        } catch (Exception e) {
            return -1;
        }
    }

    private Long safeGet(Future<Long> future) {
        try {
            return future.get();
        } catch (Exception e) {
            return -1L;
        }
    }

    private long percentile(List<Long> sorted, int p) {
        if (sorted.isEmpty()) return -1;
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
