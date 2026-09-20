package com.residuosolido.app.model;

import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad central: solicitud de recolección de residuos reciclables.
 * Encapsula el ciclo de vida (PENDING → IN_PROGRESS/COMPLETED/REJECTED),
 * datos de contacto (usuario o invitado), materiales, dirección y horario confirmado.
 */
@Document(collection = "requests")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Request {

    @Id
    private String id;

    @Version
    private Long version;

    @DocumentReference(lazy = true)
    private User user;

    @DocumentReference(lazy = true)
    @Indexed
    private User organization;

    private String guestName;
    private String guestPhone;
    private String address;
    private String addressReference;
    private City city;
    private List<MaterialCategory> materials = new ArrayList<>();
    private String estimatedWeight;
    private String estimatedVolume;
    private String imageUrl;
    private TimeSlot confirmedSlot;
    @Indexed
    private RequestStatus status = RequestStatus.PENDING;
    private LocalDateTime createdAt;
    private String trackingCode;

    public void accept(TimeSlot slot) {
        if (slot == null) throw new ValidationException(ServerMessage.ERROR_REQUEST_SLOT_REQUIRED);
        this.confirmedSlot = slot;
        this.status = status.transitionAccept();
    }

    public void complete() {
        this.status = status.transitionComplete();
    }

    public void reject() {
        this.status = status.transitionReject();
    }

    public boolean canBeEdited() { return status.canBeEdited(); }
    public boolean canBeDeleted() { return status.canBeDeleted(); }
    public boolean isGuest() { return user == null; }
    public boolean hasMaterials() { return materials != null && !materials.isEmpty(); }
    public boolean hasImage() { return imageUrl != null && !imageUrl.isBlank(); }
    public boolean isPending() { return status == RequestStatus.PENDING; }
    public boolean isInProgress() { return status == RequestStatus.IN_PROGRESS; }
    public String getContactName() { return user != null ? user.getDisplayName() : guestName; }
    public String getContactPhone() { return user != null ? user.getPhone() : guestPhone; }

    public void assignOrganization(User org) {
        if (org == null) throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        if (!org.isOrganization()) throw new ValidationException(ServerMessage.ERROR_REQUEST_ASSIGN_NOT_ORGANIZATION);
        this.organization = org;
    }

}
