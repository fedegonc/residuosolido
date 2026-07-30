package com.residuosolido.app.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BreadcrumbService {

    public List<Map<String, String>> create() {
        return new ArrayList<>();
    }

    public List<Map<String, String>> add(List<Map<String, String>> breadcrumbs, String label, String href) {
        breadcrumbs.add(Map.of("label", label, "href", href != null ? href : ""));
        return breadcrumbs;
    }

    public List<Map<String, String>> addCurrent(List<Map<String, String>> breadcrumbs, String label) {
        breadcrumbs.add(Map.of("label", label, "href", ""));
        return breadcrumbs;
    }

    public List<Map<String, String>> home() {
        return add(create(), "Inicio", "/");
    }
}
