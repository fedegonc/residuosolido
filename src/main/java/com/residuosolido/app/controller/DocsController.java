package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sirve archivos estáticos de docs/ con el content-type correcto.
 * Los .md se sirven como text/markdown y los .drawio como application/xml.
 */
@Controller
public class DocsController {

    private static final Path DOCS_DIR = Paths.get("docs").toAbsolutePath();

    @GetMapping(Routes.DOCS_FILE)
    public ResponseEntity<Resource> serveMarkdown(@PathVariable String file) {
        return serveDoc(file + ".md", MediaType.TEXT_MARKDOWN);
    }

    @GetMapping(Routes.DOCS_DIAGRAM)
    public ResponseEntity<Resource> serveDrawio(@PathVariable String file) {
        return serveDoc("diagrams/" + file + ".drawio", MediaType.APPLICATION_XML);
    }

    private ResponseEntity<Resource> serveDoc(String relativePath, MediaType mediaType) {
        try {
            File file = DOCS_DIR.resolve(relativePath).normalize().toFile();
            if (!file.exists() || !file.isFile() || !file.toPath().startsWith(DOCS_DIR)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentLength(file.length())
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
