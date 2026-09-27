package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de concurrencia REAL (no mock de la excepción) sobre el @Version de
 * Request — hueco encontrado en la auditoría "mago del parche vs. senior":
 * las 4 coberturas de "concurrencia" en RequestServiceTest son
 * `.thenThrow(new OptimisticLockingFailureException(...))` vía Mockito, no
 * una carrera real de 2 threads escribiendo el mismo documento.
 *
 * Este test usa 2 threads reales sincronizados con CountDownLatch, contra
 * MongoDB Atlas real (no mock), guardando el MISMO documento con datos
 * cargados en instantes distintos — exactamente el escenario que produce
 * el conflicto en producción (dos requests HTTP concurrentes aceptando/
 * editando la misma solicitud).
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.database=residuosolido_test_concurrency",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class RequestConcurrencyTest {

    @Autowired
    private RequestRepository requestRepository;

    private String requestId;

    @AfterEach
    void cleanup() {
        if (requestId != null) {
            requestRepository.deleteById(requestId);
        }
    }

    @Test
    void concurrentSaves_onlyOneSucceeds_otherThrowsOptimisticLock() throws Exception {
        Request seed = Request.forGuest("Concurrencia Test", "+59899000000", "CONCTEST");
        seed.updateDraft(City.RIVERA, "Calle Original 1", null, List.of(MaterialCategory.PLASTICO));
        requestId = requestRepository.save(seed).getId();

        CountDownLatch bothLoaded = new CountDownLatch(2);
        CountDownLatch goSave = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        Runnable loadMutateAndSave = () -> {
            try {
                Request copy = requestRepository.findById(requestId).orElseThrow();
                copy.updateDraft(City.RIVERA, "Calle Editada por " + Thread.currentThread().getName(),
                        null, List.of(MaterialCategory.VIDRIO));
                bothLoaded.countDown();
                goSave.await(5, TimeUnit.SECONDS);
                requestRepository.save(copy);
                successes.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                conflicts.incrementAndGet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        var f1 = pool.submit(loadMutateAndSave);
        var f2 = pool.submit(loadMutateAndSave);

        assertTrue(bothLoaded.await(5, TimeUnit.SECONDS),
                "Los 2 threads deben cargar SU PROPIA copia antes de que ninguno guarde");
        goSave.countDown();
        f1.get(5, TimeUnit.SECONDS);
        f2.get(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, successes.get(), "Exactamente un thread debe ganar la carrera");
        assertEquals(1, conflicts.get(), "El otro debe fallar con OptimisticLockingFailureException real, no mockeada");
    }
}
