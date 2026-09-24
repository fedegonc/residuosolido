package com.residuosolido.app.service;

import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class LocalImageServiceTest {

    @TempDir
    Path tempDir;

    private LocalImageService service;
    private RequestRepository requestRepository;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        service = new LocalImageService(tempDir.toString(), requestRepository);
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
        assertTrue(ex.getMessage().contains("error.image.invalid_type"));
    }

    @Test
    void uploadFile_disallowedExtensionWithImageContentType_throws() {
        MockMultipartFile exe = new MockMultipartFile(
                "imageFile", "payload.exe", "image/png", "MZ".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.uploadFile(exe));
        assertTrue(ex.getMessage().contains("error.image.invalid_extension"));
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
        assertTrue(ex.getMessage().contains("error.image.too_large"));
    }

    @Test
    void uploadFile_generatesUniqueFilenames() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "foto.png", "image/png", "bytes".getBytes());

        String url1 = service.uploadFile(file);
        String url2 = service.uploadFile(file);

        assertNotEquals(url1, url2);
    }

    // ===== attachImageToRequest =====

    @Test
    void attachImageToRequest_nullOrEmpty_returnsRequestUnchanged() {
        com.residuosolido.app.model.Request request = new com.residuosolido.app.model.Request();

        assertSame(request, service.attachImageToRequest(request, null));

        MockMultipartFile empty = new MockMultipartFile(
                "imageFile", "foto.jpg", "image/jpeg", new byte[0]);
        assertSame(request, service.attachImageToRequest(request, empty));
        assertNull(request.getImageUrl());
    }

    @Test
    void attachImageToRequest_validFile_savesRequestWithUrl() {
        com.residuosolido.app.model.Request request = new com.residuosolido.app.model.Request();
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "foto.png", "image/png", "bytes".getBytes());
        when(requestRepository.save(request)).thenAnswer(inv -> inv.getArgument(0));

        com.residuosolido.app.model.Request result = service.attachImageToRequest(request, file);

        assertNotNull(result.getImageUrl());
        assertTrue(result.getImageUrl().startsWith("/uploads/"));
        verify(requestRepository).save(request);
    }

    @Test
    void attachImageToRequest_uploadFails_throwsStateException() {
        com.residuosolido.app.model.Request request = new com.residuosolido.app.model.Request();
        MockMultipartFile invalid = new MockMultipartFile(
                "imageFile", "doc.pdf", "application/pdf", "pdf".getBytes());

        assertThrows(com.residuosolido.app.exception.StateException.class,
                () -> service.attachImageToRequest(request, invalid));
    }
}
