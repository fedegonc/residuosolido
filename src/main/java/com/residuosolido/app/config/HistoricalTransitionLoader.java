package com.residuosolido.app.config;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatusTransition;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.RequestStatusTransitionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Migración one-time: crea transiciones históricas basadas en estado actual + createdAt.
 * Después de ejecutarse, se puede borrar o dejar como idempotent.
 */
@Component
public class HistoricalTransitionLoader implements CommandLineRunner {

    private final RequestRepository requestRepository;
    private final RequestStatusTransitionRepository transitionRepository;

    public HistoricalTransitionLoader(RequestRepository requestRepository,
                                     RequestStatusTransitionRepository transitionRepository) {
        this.requestRepository = requestRepository;
        this.transitionRepository = transitionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Request> allRequests = requestRepository.findAll();
        List<RequestStatusTransition> toCreate = new ArrayList<>();

        for (Request request : allRequests) {
            long existingCount = transitionRepository.findByRequestIdOrderByTimestampAsc(request.getId()).size();
            if (existingCount == 0) {
                // Crear transición inicial: PENDING → estado actual
                RequestStatusTransition initialTransition = new RequestStatusTransition(
                        request,
                        RequestStatus.PENDING,
                        request.getStatus(),
                        request.getOrganization()
                );
                initialTransition.setTimestamp(request.getCreatedAt() != null ?
                        request.getCreatedAt().plusSeconds(1) : LocalDateTime.now());
                toCreate.add(initialTransition);
            }
        }

        if (!toCreate.isEmpty()) {
            transitionRepository.saveAll(toCreate);
        }
    }
}
