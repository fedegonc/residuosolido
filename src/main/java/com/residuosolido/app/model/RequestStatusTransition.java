package com.residuosolido.app.model;

import com.residuosolido.app.enums.RequestStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document(collection = "request_status_transitions")
public class RequestStatusTransition {

    @Id
    private String id;

    @DocumentReference
    private Request request;
    private String requestId;

    private RequestStatus fromStatus;
    private RequestStatus toStatus;
    private LocalDateTime timestamp;

    @DocumentReference
    private Organization organization;

    public RequestStatusTransition() {}

    public RequestStatusTransition(Request request, RequestStatus fromStatus, RequestStatus toStatus,
                                   Organization organization) {
        this.request = request;
        this.requestId = request != null ? request.getId() : null;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.timestamp = LocalDateTime.now();
        this.organization = organization;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
        this.requestId = request != null ? request.getId() : null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(RequestStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public RequestStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(RequestStatus toStatus) {
        this.toStatus = toStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
