package com.residuosolido.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalImageServiceTest {

    @TempDir
    Path tempDir;

    private LocalImageService service;

    @BeforeEach
    void setUp() {
        service = new LocalImageService(tempDir.toString());
    }

    @Test
    void uploadFile_validJpeg_savesAndReturnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "foto.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        String url = service.uploadFile(file);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/"));
        assertTrue(url.endsWith(".jpg"));
        String filename = url.substring("/uploads/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void uploadFile_nullOrEmpty_returnsNull() throws Exception {
        assertNull(service.uploadFile(null));
        MockMultipartFile empty = new MockMultipartFile(
                "imageFile", "foto.jpg", "image/jpeg", new byte[0]);
        assertNull(service.uploadFile(empty));
    }

    @Test
    void uploadFile_disallowedContentType_throws() {
        MockMultipartFile html = new MockMultipartFile(
                "imageFile", "malicioso.html", "text/html", "<script>alert(1)</script>".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadFile(html));
        assertTrue(ex.getMessage().contains("Tipo de archivo no permitido"));
    }

    @Test
    void uploadFile_disallowedExtensionWithImageContentType_throws() {
        MockMultipartFile exe = new MockMultipartFile(
                "imageFile", "payload.exe", "image/png", "MZ".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadFile(exe));
        assertTrue(ex.getMessage().contains("Extensión de archivo no permitida"));
    }

    @Test
    void uploadFile_missingExtension_throws() {
        MockMultipartFile noExt = new MockMultipartFile(
                "imageFile", "sinextension", "image/png", "png-bytes".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.uploadFile(noExt));
    }

    @Test
    void uploadFile_oversizedFile_throws() {
        byte[] big = new byte[11 * 1024 * 1024];
        MockMultipartFile huge = new MockMultipartFile(
                "imageFile", "grande.jpg", "image/jpeg", big);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadFile(huge));
        assertTrue(ex.getMessage().contains("tamaño máximo"));
    }

    @Test
    void uploadFile_generatesUniqueFilenames() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "foto.png", "image/png", "bytes".getBytes());

        String url1 = service.uploadFile(file);
        String url2 = service.uploadFile(file);

        assertNotEquals(url1, url2);
    }
}
