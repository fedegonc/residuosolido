package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servidor de imágenes con validación de propiedad.
 *
 * Las imágenes están almacenadas en disk (uploadDir) pero se sirven únicamente
 * a través de este controller autenticado. Solo el dueño de la solicitud o la
 * organización receptora pueden acceder.
 *
 * Esto previene que cualquier persona acceda a imágenes de terceros simplemente
 * adivinando nombres de archivo.
 */
@Controller
@PreAuthorize("hasRole('USER') or hasRole('ORGANIZATION')")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    private final String uploadDir;
    private final RequestRepository requestRepository;

    public ImageController(@Value("${app.upload.dir:uploads}") String uploadDir,
                         RequestRepository requestRepository) {
        this.uploadDir = uploadDir;
        this.requestRepository = requestRepository;
    }

    /**
     * Sirve una imagen de solicitud si el usuario tiene acceso.
     *
     * Validación: el usuario debe ser el dueño de la solicitud (citizen) o
     * la organización receptora.
     */
    @GetMapping(Routes.IMAGE_BY_ID)
    public ResponseEntity<?> serveImage(@PathVariable String filename, @CurrentUser User user) {
        try {
            // Validar nombre de archivo: debe ser UUID + extension
            if (!isValidFilename(filename)) {
                logger.warn("Intento de acceso a imagen con nombre inválido: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Buscar solicitud que contiene esta imagen
            Request request = requestRepository.findAll().stream()
                    .filter(r -> r.getImageUrl() != null && r.getImageUrl().endsWith(filename))
                    .findFirst()
                    .orElse(null);

            if (request == null) {
                logger.warn("Imagen no encontrada en solicitud: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Validar acceso: dueño (usuario) o receptora (organización)
            boolean isOwner = request.getUser().getId().equals(user.getId());
            boolean isRecipient = request.getOrganization() != null &&
                    request.getOrganization().getId().equals(user.getId());

            if (!isOwner && !isRecipient) {
                logger.warn("Acceso denegado a imagen: usuario {} no es dueño ni receptora de solicitud {}",
                        user.getId(), request.getId());
                return ResponseEntity.status(403).build();
            }

            // Servir imagen
            Path imagePath = Paths.get(uploadDir).resolve(filename);
            if (!Files.exists(imagePath)) {
                logger.warn("Archivo de imagen no existe en disk: {}", filename);
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = Files.readAllBytes(imagePath);
            MediaType mediaType = getMediaType(filename);

            logger.debug("Sirviendo imagen {} para usuario {}", filename, user.getUsername());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageData);

        } catch (IOException e) {
            logger.error("Error al leer imagen {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    private boolean isValidFilename(String filename) {
        // Validar formato: UUID (36 chars) + extension (4-5 chars)
        // Prevenir path traversal: solo alfanuméricos, guiones, puntos
        return filename != null && filename.matches("^[a-f0-9\\-]{36}\\.(jpg|jpeg|png|gif|webp)$");
    }

    private MediaType getMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return new MediaType("image", "webp");
        return MediaType.IMAGE_JPEG; // default para .jpg, .jpeg
    }
}
