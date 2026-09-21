package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.config.Routes;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sirve archivos estáticos de docs/ con el content-type correcto.
 * Los .md se sirven como text/markdown y los .drawio como application/xml.
 */
@Controller
public class DocsController {

    private static final Path DOCS_DIR = Paths.get("docs").toAbsolutePath();
    private static final Path SCRATCH_DIR = Paths.get("scratch").toAbsolutePath();
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final List<String[]> DIAGRAMS = List.of(
            new String[]{"figura1-casos-uso", "Casos de uso"},
            new String[]{"figura2-modelo-logico", "Modelo lógico / ER"},
            new String[]{"figura3-clases", "Diagrama de clases"},
            new String[]{"figura4-secuencia", "Diagrama de secuencia"},
            new String[]{"figura4-estados", "Diagrama de estados"},
            new String[]{"figura5-gitflow", "Gitflow del proyecto"}
    );

    @GetMapping(Routes.DOCS_FILE)
    public ResponseEntity<Resource> serveMarkdown(@PathVariable String file) {
        return serveFile(DOCS_DIR, file + ".md", MediaType.TEXT_MARKDOWN);
    }

    @GetMapping(Routes.DOCS_DIAGRAM)
    public ResponseEntity<Resource> serveDrawio(@PathVariable String file) {
        return serveFile(DOCS_DIR, "diagrams/" + file + ".drawio", MediaType.APPLICATION_XML);
    }

    /**
     * Sirve scratch/{App,pseudoapp}.java como texto plano para leer desde el navegador.
     * scratch/ está gitignoreado a propósito (ver .gitignore) — esto funciona en local/dev
     * porque lee del disco, pero da 404 en Render porque el archivo nunca llega a subirse.
     */
    @GetMapping(Routes.SCRATCH_FILE)
    public ResponseEntity<Resource> serveScratch(@PathVariable String file) {
        return serveFile(SCRATCH_DIR, file + ".java", MediaType.TEXT_PLAIN);
    }

    /**
     * Visor embebido: lee cada .drawio del disco y lo pasa como XML inline al
     * script oficial de draw.io (viewer-static.min.js), que lo renderiza en el
     * cliente sin necesitar que draw.io haga fetch a este servidor (evita CORS).
     */
    @GetMapping(Routes.DOCS_DIAGRAMS_VIEW)
    public String viewDiagrams(Model model) {
        List<Map<String, String>> diagrams = DIAGRAMS.stream()
                .map(d -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("title", d[1]);
                    entry.put("mxgraph", toMxgraphAttr(d[0]));
                    return entry;
                })
                .filter(entry -> entry.get("mxgraph") != null)
                .toList();
        model.addAttribute("diagrams", diagrams);
        return "docs/diagramas";
    }

    private String toMxgraphAttr(String baseName) {
        try {
            File file = DOCS_DIR.resolve("diagrams/" + baseName + ".drawio").normalize().toFile();
            if (!file.exists() || !file.isFile() || !file.toPath().startsWith(DOCS_DIR)) {
                return null;
            }
            String xml = Files.readString(file.toPath());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("xml", xml);
            payload.put("toolbar", "zoom layers lightbox");
            payload.put("resize", true);
            return JSON.writeValueAsString(payload);
        } catch (Exception e) {
            return null;
        }
    }

    private ResponseEntity<Resource> serveFile(Path baseDir, String relativePath, MediaType mediaType) {
        try {
            File file = baseDir.resolve(relativePath).normalize().toFile();
            if (!file.exists() || !file.isFile() || !file.toPath().startsWith(baseDir)) {
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
