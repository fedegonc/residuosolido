package com.residuosolido.app.service;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalImageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageService.class);

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB, alineado con multipart config
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final String uploadDir;

    private final RequestRepository requestRepository;

    @Autowired
    public LocalImageService(@Value("${app.upload.dir:uploads}") String uploadDir,
                             RequestRepository requestRepository) {
        this.uploadDir = uploadDir;
        this.requestRepository = requestRepository;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("error.image.too_large");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("error.image.invalid_type");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("error.image.invalid_extension");
        }

        String filename = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        logger.info("Imagen guardada localmente: {}", filename);
        return "/uploads/" + filename;
    }

    public Request attachImageToRequest(Request request, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                request.setImageUrl(uploadFile(imageFile));
                return requestRepository.save(request);
            } catch (Exception e) {
                logger.warn("Error al subir imagen de solicitud: {}", e.getMessage());
                throw new IllegalStateException("flash.request.image_upload_failed", e);
            }
        }
        return request;
    }
}
