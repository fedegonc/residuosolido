package com.residuosolido.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_requests")
public class PasswordResetRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String maskedEmail;
    
    @Column(nullable = false)
    private String lastKnownPassword;
    
    @Column(nullable = false)
    private LocalDateTime requestDate;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    
    private String adminNotes;
    
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
    
    // Constructors
    public PasswordResetRequest() {}
    
    public PasswordResetRequest(String maskedEmail, String lastKnownPassword) {
        this.maskedEmail = maskedEmail;
        this.lastKnownPassword = lastKnownPassword;
        this.requestDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMaskedEmail() { return maskedEmail; }
    public void setMaskedEmail(String maskedEmail) { this.maskedEmail = maskedEmail; }
    
    public String getLastKnownPassword() { return lastKnownPassword; }
    public void setLastKnownPassword(String lastKnownPassword) { this.lastKnownPassword = lastKnownPassword; }
    
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
}
