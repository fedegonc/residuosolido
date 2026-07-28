package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "requests")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Request {

    @Id
    private String id;

    @DBRef
    private User user;

    @DBRef
    private User organization;

    private String guestName;
    private String guestPhone;

    private String address;
    private String addressReference;

    private City city;

    private List<MaterialCategory> materials = new ArrayList<>();

    private String imageUrl;

    private TimeSlot confirmedSlot;

    private RequestStatus status = RequestStatus.PENDING;

    private LocalDateTime createdAt;

    public void accept(TimeSlot slot) {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("request.error.accept_not_pending");
        }
        if (slot == null) {
            throw new IllegalArgumentException("request.error.slot_required");
        }
        this.confirmedSlot = slot;
        this.status = RequestStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("request.error.complete_not_in_progress");
        }
        this.status = RequestStatus.COMPLETED;
    }

    public void reject() {
        if (status != RequestStatus.PENDING && status != RequestStatus.IN_PROGRESS) {
            throw new IllegalStateException("request.error.reject_invalid_state");
        }
        this.status = RequestStatus.REJECTED;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isInProgress() {
        return status == RequestStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == RequestStatus.COMPLETED;
    }

    public boolean isRejected() {
        return status == RequestStatus.REJECTED;
    }

    public boolean canBeEdited() {
        return status == RequestStatus.PENDING;
    }

    public boolean canBeDeleted() {
        return status == RequestStatus.PENDING;
    }

    public boolean isGuest() {
        return user == null;
    }

    public void setRequester(User user, String guestName, String guestPhone) {
        if (user != null) {
            this.user = user;
        } else {
            this.guestName = guestName;
            this.guestPhone = guestPhone;
        }
    }

    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isBlank();
    }

    public boolean hasMaterials() {
        return materials != null && !materials.isEmpty();
    }

    public void assignOrganization(User org) {
        if (org == null) {
            throw new IllegalArgumentException("request.error.organization_required");
        }
        if (!org.isOrganization()) {
            throw new IllegalArgumentException("request.error.assign_not_organization");
        }
        this.organization = org;
    }

    public void updateDetails(City city, String address, String addressReference,
                              List<MaterialCategory> materials) {
        if (!canBeEdited()) {
            throw new IllegalStateException("flash.request.edit.pending_only");
        }
        this.city = city;
        this.address = address;
        this.addressReference = addressReference;
        this.materials = materials != null ? materials : List.of();
    }

    public List<TimeSlot> getProposedSlots() {
        return Arrays.asList(TimeSlot.values());
    }

    public String getContactName() {
        return user != null ? user.getDisplayName() : guestName;
    }

    public String getContactPhone() {
        return user != null ? user.getPhone() : guestPhone;
    }

    public boolean hasValidContact() {
        return getContactName() != null && !getContactName().isBlank()
            && getContactPhone() != null && !getContactPhone().isBlank();
    }
}
