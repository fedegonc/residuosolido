package com.residuosolido.app.service;

import com.residuosolido.app.repository.*;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataCleanupService {
    private static final Logger log = LoggerFactory.getLogger(DataCleanupService.class);

    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final NotificationRepository notificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final WasteSectionRepository wasteSectionRepository;
    private final MaterialRepository materialRepository;
    private final PhoneRepository phoneRepository;
    private final EntityManager entityManager;

    public DataCleanupService(
            FeedbackRepository feedbackRepository,
            RequestRepository requestRepository,
            PasswordResetRequestRepository passwordResetRequestRepository,
            NotificationRepository notificationRepository,
            ChatMessageRepository chatMessageRepository,
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            WasteSectionRepository wasteSectionRepository,
            MaterialRepository materialRepository,
            PhoneRepository phoneRepository,
            EntityManager entityManager
    ) {
        this.feedbackRepository = feedbackRepository;
        this.requestRepository = requestRepository;
        this.passwordResetRequestRepository = passwordResetRequestRepository;
        this.notificationRepository = notificationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.wasteSectionRepository = wasteSectionRepository;
        this.materialRepository = materialRepository;
        this.phoneRepository = phoneRepository;
        this.entityManager = entityManager;
    }

    /**
     * Limpia datos funcionales sin tocar usuarios para no perder acceso.
     */
    @Transactional
    public void cleanupAll() {
        log.warn("[Cleanup] Iniciando limpieza temporal de datos (sin usuarios)...");

        // Dependientes primero
        chatMessageRepository.deleteAllInBatch();
        notificationRepository.deleteAllInBatch();
        passwordResetRequestRepository.deleteAllInBatch();
        feedbackRepository.deleteAllInBatch();
        requestRepository.deleteAllInBatch();

        // Contenido
        postRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        // Catálogo/relaciones
        materialRepository.deleteAllInBatch();
        phoneRepository.deleteAllInBatch();
        wasteSectionRepository.deleteAllInBatch();

        log.warn("[Cleanup] Limpieza completada.");
    }

    /**
     * Limpia inconsistencias en la tabla de unión waste_section_categories:
     * - Elimina filas huérfanas (waste_section_id o category_id inexistentes)
     * - Elimina duplicados en la combinación (waste_section_id, category_id)
     */
    @Transactional
    public void cleanupWasteSectionCategories() {
        log.warn("[Cleanup] Corrigiendo FK/duplicados en waste_section_categories...");

        // Huérfanos por waste_section_id
        int orphanWaste = entityManager.createNativeQuery(
                "DELETE FROM waste_section_categories WHERE waste_section_id NOT IN (SELECT id FROM waste_sections)")
            .executeUpdate();

        // Huérfanos por category_id
        int orphanCat = entityManager.createNativeQuery(
                "DELETE FROM waste_section_categories WHERE category_id NOT IN (SELECT id FROM categories)")
            .executeUpdate();

        // Duplicados manteniendo una fila (usa CTE compatible con PostgreSQL)
        int duplicates = entityManager.createNativeQuery(
            "WITH ranked AS (\n" +
            "  SELECT ctid, waste_section_id, category_id,\n" +
            "         ROW_NUMBER() OVER (PARTITION BY waste_section_id, category_id ORDER BY waste_section_id) AS rn\n" +
            "  FROM waste_section_categories\n" +
            ")\n" +
            "DELETE FROM waste_section_categories w USING ranked r\n" +
            "WHERE w.ctid = r.ctid AND r.rn > 1")
            .executeUpdate();

        log.warn("[Cleanup] waste_section_categories: huérfanos(waste)={}, huérfanos(cat)={}, duplicados={}.", orphanWaste, orphanCat, duplicates);
    }
}
