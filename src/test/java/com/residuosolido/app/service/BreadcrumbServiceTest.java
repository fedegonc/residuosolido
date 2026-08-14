package com.residuosolido.app.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BreadcrumbServiceTest {

    private final BreadcrumbService service = new BreadcrumbService();

    @Test
    void create_returnsEmptyList() {
        assertTrue(service.create().isEmpty());
    }

    @Test
    void add_appendsBreadcrumbWithLabelAndHref() {
        List<Map<String, String>> breadcrumbs = service.add(service.create(), "Solicitudes", "/solicitudes");
        assertEquals(1, breadcrumbs.size());
        assertEquals("Solicitudes", breadcrumbs.get(0).get("label"));
        assertEquals("/solicitudes", breadcrumbs.get(0).get("href"));
    }

    @Test
    void add_nullHref_defaultsToEmptyString() {
        List<Map<String, String>> breadcrumbs = service.add(service.create(), "Inicio", null);
        assertEquals("", breadcrumbs.get(0).get("href"));
    }

    @Test
    void addCurrent_appendsBreadcrumbWithEmptyHref() {
        List<Map<String, String>> breadcrumbs = service.addCurrent(service.create(), "Detalle");
        assertEquals(1, breadcrumbs.size());
        assertEquals("Detalle", breadcrumbs.get(0).get("label"));
        assertEquals("", breadcrumbs.get(0).get("href"));
    }

    @Test
    void home_returnsSingleBreadcrumbPointingToRoot() {
        List<Map<String, String>> breadcrumbs = service.home();
        assertEquals(1, breadcrumbs.size());
        assertEquals("Inicio", breadcrumbs.get(0).get("label"));
        assertEquals("/", breadcrumbs.get(0).get("href"));
    }

    @Test
    void add_multipleTimes_accumulatesInOrder() {
        List<Map<String, String>> breadcrumbs = service.create();
        service.add(breadcrumbs, "Inicio", "/");
        service.add(breadcrumbs, "Solicitudes", "/solicitudes");
        service.addCurrent(breadcrumbs, "Detalle");
        assertEquals(3, breadcrumbs.size());
        assertEquals("Inicio", breadcrumbs.get(0).get("label"));
        assertEquals("Solicitudes", breadcrumbs.get(1).get("label"));
        assertEquals("Detalle", breadcrumbs.get(2).get("label"));
    }
}
