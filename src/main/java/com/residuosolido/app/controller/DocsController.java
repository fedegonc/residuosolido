package com.residuosolido.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.config.Routes;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

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
    private static final org.commonmark.parser.Parser MARKDOWN_PARSER =
            org.commonmark.parser.Parser.builder().build();
    private static final org.commonmark.renderer.html.HtmlRenderer HTML_RENDERER =
            org.commonmark.renderer.html.HtmlRenderer.builder().escapeHtml(true).build();

    private static final List<String[]> DIAGRAMS = List.of(
            new String[]{"figura1-casos-uso", "Casos de uso"},
            new String[]{"figura2-modelo-logico", "Modelo lógico / ER"},
            new String[]{"figura3-clases", "Diagrama de clases"},
            new String[]{"figura4-secuencia", "Diagrama de secuencia"},
            new String[]{"figura4-estados", "Diagrama de estados"},
            new String[]{"figura5-gitflow", "Gitflow del proyecto"},
            new String[]{"figura6-notificaciones", "Secuencia — aceptar y notificar"}
    );

    @GetMapping(Routes.DOCS_FILE)
    public ResponseEntity<Resource> serveMarkdown(@PathVariable String file) {
        return serveFile(DOCS_DIR, file + ".md", MediaType.TEXT_MARKDOWN);
    }

    /**
     * Vista HTML del .md: mismo archivo que {@link #serveMarkdown} pero parseado
     * con CommonMark y envuelto en el layout. escapeHtml evita que HTML crudo
     * embebido en el markdown se inyecte tal cual (los .md son contenido del
     * repo, pero una tabla con markup accidental no debe romperse tampoco).
     */
    @GetMapping(Routes.DOCS_VIEW)
    public String viewMarkdown(@PathVariable String file, Model model) {
        File doc = DOCS_DIR.resolve(file + ".md").normalize().toFile();
        if (!doc.exists() || !doc.isFile() || !doc.toPath().startsWith(DOCS_DIR)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doc no encontrado: " + file);
        }
        try {
            org.commonmark.node.Node document = MARKDOWN_PARSER.parse(Files.readString(doc.toPath()));
            model.addAttribute("docName", file);
            model.addAttribute("docHtml", HTML_RENDERER.render(document));
            return "docs/markdown";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doc no encontrado: " + file);
        }
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

    /**
     * El hub mostraba conteos hardcodeados (tests, filas de MEJORAS) que
     * quedaban desactualizados apenas se agregaba una mejora — se detectó un
     * drift real: "308 tests"/"146 implementadas" cuando ya había 453
     * tests y 141+ filas. docs/MEJORAS.md viaja en el contenedor de
     * producción (ver Dockerfile: COPY docs ./docs), así que se puede leer
     * en runtime real, no solo en dev.
     *
     * Los tests NO se cuentan acá a propósito: el build de Docker corre con
     * -Dmaven.test.skip=true (deploys rápidos, sin pegarle a Mongo Atlas
     * durante el build), así que no hay ningún artefacto de test real
     * disponible en el contenedor desplegado — cualquier número ahí sería
     * tan hardcodeado como el que reemplaza. Se prefiere no mostrar un
     * número falso antes que mostrar uno viejo (mismo criterio que ya usan
     * las cards de Jacoco/PMD: instrucción para ejecutar, no un valor fijo).
     */
    @GetMapping(Routes.DOCS_HUB)
    public String viewHub(Model model) {
        model.addAttribute("diagrams", DIAGRAMS);
        model.addAttribute("mejorasStats", countMejorasByEstado());
        return "docs/hub";
    }

    private Map<String, Long> countMejorasByEstado() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("implementado", 0L);
        counts.put("descartado", 0L);
        counts.put("diferido", 0L);
        File mejoras = DOCS_DIR.resolve("MEJORAS.md").toFile();
        if (!mejoras.isFile()) return counts;
        try {
            for (String line : Files.readAllLines(mejoras.toPath())) {
                String[] cols = line.split("\\|");
                if (cols.length < 4 || !cols[1].trim().matches("\\d+")) continue;
                String estado = cols[3].trim().replace("*", "").toLowerCase();
                if (estado.startsWith("implementado")) counts.merge("implementado", 1L, Long::sum);
                else if (estado.startsWith("descartado") || estado.startsWith("retirado")) counts.merge("descartado", 1L, Long::sum);
                else if (estado.startsWith("diferido")) counts.merge("diferido", 1L, Long::sum);
            }
        } catch (java.io.IOException e) {
            // Sin datos frescos, el hub muestra 0/0/0 en vez de romper la página.
        }
        return counts;
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
