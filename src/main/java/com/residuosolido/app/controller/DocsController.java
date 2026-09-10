package com.residuosolido.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Páginas públicas de documentación del proyecto.
 * /documentos — índice de la documentación técnica (docs/*.md)
 * /diagramas — visor de diagramas UML (docs/diagrams/*.drawio)
 *
 * El contenido es estático para el MVP: lista los archivos que existen
 * en docs/ y docs/diagrams/ sin servirlos dinámicamente.
 */
@Controller
public class DocsController {

    private static final List<Map<String, String>> DOCUMENTS = List.of(
            doc("CORE", "Núcleo del sistema", "Visión general, stack y decisiones de arquitectura.", "fa-solid fa-cube"),
            doc("RF-RN", "Requisitos y reglas de negocio", "8 requisitos funcionales y 14 reglas de negocio (RN-01 a RN-14).", "fa-solid fa-list-check"),
            doc("ENDPOINTS", "Endpoints HTTP", "Rutas extraídas de los controllers, agrupadas por rol.", "fa-solid fa-route"),
            doc("METODOLOGIA", "Metodología", "Modelo iterativo incremental en 4 fases.", "fa-solid fa-diagram-project"),
            doc("TRADEOFFS", "Tradeoffs de diseño", "Decisiones de diseño y sus consecuencias.", "fa-solid fa-scale-balanced"),
            doc("HARDENING", "Endurecimiento del MVP", "Correcciones de seguridad aplicadas y limitaciones.", "fa-solid fa-shield-halved"),
            doc("TESTING", "Testing", "Estrategia de pruebas por capa (193 tests, JUnit 5 + Mockito).", "fa-solid fa-vial"),
            doc("MEJORAS", "Superficies de mejora", "Tabla centralizada de mejoras posibles y su estado.", "fa-solid fa-table-list"),
            doc("COPIES", "Copies del sistema", "Microcopy y textos de interfaz.", "fa-solid fa-align-left"),
            doc("SUPERFICIES", "Superficies de diseño", "Sistema de diseño canónico (variables CSS, BEM).", "fa-solid fa-palette")
    );

    private static final List<Map<String, String>> DIAGRAMS = List.of(
            diagram("figura1-casos-uso", "Figura 1 — Casos de Uso",
                    "Ocho objetivos funcionales y tres actores (invitado, usuario, organización).",
                    "fa-solid fa-users"),
            diagram("figura2-modelo-logico", "Figura 2 — Modelo Lógico",
                    "Entidades y asociaciones con multiplicidades (MongoDB).",
                    "fa-solid fa-database"),
            diagram("figura3-clases", "Figura 3 — Clases de Dominio",
                    "Vista simplificada de User, Request e InformalCollector.",
                    "fa-solid fa-sitemap"),
            diagram("figura4-estados", "Figura 4 — Diagrama de Estados",
                    "Ciclo de vida de Request: PENDING → IN_PROGRESS/REJECTED → COMPLETED.",
                    "fa-solid fa-arrows-turn-right")
    );

    private static Map<String, String> doc(String name, String title, String desc, String icon) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("title", title);
        m.put("desc", desc);
        m.put("icon", icon);
        return m;
    }

    private static Map<String, String> diagram(String file, String title, String desc, String icon) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("file", file);
        m.put("title", title);
        m.put("desc", desc);
        m.put("icon", icon);
        return m;
    }

    @GetMapping("/documentos")
    public String docsIndex(Model model) {
        model.addAttribute("documents", DOCUMENTS);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Documentos", "href", "")
        ));
        return "public/docs";
    }

    @GetMapping("/diagramas")
    public String diagramsIndex(Model model) {
        model.addAttribute("diagrams", DIAGRAMS);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Diagramas", "href", "")
        ));
        return "public/diagrams";
    }
}
