package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatusTransition;
import com.residuosolido.app.repository.RequestStatusTransitionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestStatusTransitionService {

    private final RequestStatusTransitionRepository transitionRepository;

    public RequestStatusTransitionService(RequestStatusTransitionRepository transitionRepository) {
        this.transitionRepository = transitionRepository;
    }

    public void recordTransition(Request request, RequestStatus fromStatus, RequestStatus toStatus, Organization organization) {
        if (request == null || fromStatus == null || toStatus == null) {
            return;
        }
        RequestStatusTransition transition = new RequestStatusTransition(request, fromStatus, toStatus, organization);
        transitionRepository.save(transition);
    }

    public List<RequestStatusTransition> getTransitionsForRequest(String requestId) {
        return transitionRepository.findByRequestIdOrderByTimestampAsc(requestId);
    }

    public int countTransitionsToStatusInMonth(Organization org, RequestStatus targetStatus, YearMonth month) {
        if (org == null || targetStatus == null) {
            return 0;
        }
        LocalDateTime startOfMonth = month.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = month.plusMonths(1).atDay(1).atStartOfDay();

        List<RequestStatusTransition> transitions = transitionRepository
                .countTransitionsForMonth(org.getId(), targetStatus, startOfMonth, endOfMonth);
        return transitions.size();
    }

    public int countAcceptedInMonth(Organization org, YearMonth month) {
        return countTransitionsToStatusInMonth(org, RequestStatus.IN_PROGRESS, month);
    }

    public int countRejectedInMonth(Organization org, YearMonth month) {
        return countTransitionsToStatusInMonth(org, RequestStatus.REJECTED, month);
    }

    public int countCompletedInMonth(Organization org, YearMonth month) {
        return countTransitionsToStatusInMonth(org, RequestStatus.COMPLETED, month);
    }
}
