package com.residuosolido.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "system_texts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"code", "language"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code; // Ej: LOGIN_TITLE, LOGIN_BUTTON, etc.

    @Column(nullable = false, length = 5)
    private String language; // 'es', 'pt'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // El texto traducido
}
