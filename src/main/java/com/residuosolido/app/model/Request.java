package com.residuosolido.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "requests")
@Getter
@Setter
@NoArgsConstructor
public class Request {

    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private User organization;

    private String description;

    @DBRef
    private List<Material> materials = new ArrayList<>();

    private String collectionAddress;

    private BigDecimal collectionLatitude;
    private BigDecimal collectionLongitude;

    private LocalDate scheduledDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private RequestStatus status = RequestStatus.PENDING;

    private String notes;

    private String imageUrl;

    private BigDecimal quantityKg;

    public void accept() {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException("Solo se pueden aceptar solicitudes pendientes");
        this.status = RequestStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != RequestStatus.IN_PROGRESS)
            throw new IllegalStateException("Solo se pueden completar solicitudes en proceso");
        this.status = RequestStatus.COMPLETED;
    }

    public void reject() {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException("Solo se pueden rechazar solicitudes pendientes");
        this.status = RequestStatus.REJECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
