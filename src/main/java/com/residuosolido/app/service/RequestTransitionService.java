package com.residuosolido.app.service;

import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

/**
 * Orquesta las transiciones de estado de las solicitudes:
 * PENDING → IN_PROGRESS (aceptar), PENDING → REJECTED (rechazar),
 * IN_PROGRESS → COMPLETED (completar).
 */
@Service
public class RequestTransitionService {

    private final RequestRepository requestRepository;
    private final RequestQueryService requestQueryService;

    public RequestTransitionService(RequestRepository requestRepository,
                                    RequestQueryService requestQueryService) {
        this.requestRepository = requestRepository;
        this.requestQueryService = requestQueryService;
    }

    public void acceptRequest(String id, User org, TimeSlot slot) {
        Request request = requestQueryService.getOwnedOrgRequest(id, org);
        request.accept(slot);
        saveWithOptimisticLock(request);
    }

    public void rejectRequest(String id, User org) {
        Request request = requestQueryService.getOwnedOrgRequest(id, org);
        request.reject();
        saveWithOptimisticLock(request);
    }

    public void completeRequest(String id, User org) {
        Request request = requestQueryService.getOwnedOrgRequest(id, org);
        request.complete();
        saveWithOptimisticLock(request);
    }

    private void saveWithOptimisticLock(Request request) {
        try {
            requestRepository.save(request);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("flash.request.concurrent_modification", e);
        }
    }
}
