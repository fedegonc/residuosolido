package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.util.PageContentLoader;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;
import java.util.Map;

/**
 * Páginas de contenido público genéricas, direccionadas por slug (reemplaza el
 * @GetMapping("/sobre-catadores") hardcodeado — ver docs/MEJORAS.md #179).
 *
 * Contenido bilingüe en pages-{es,pt}.json, cargado por PageContentLoader
 * (mismo patrón que LandingCardLoader). Agregar una página nueva = 1 entrada
 * en ese JSON, sin tocar Routes, SecurityConfig ni este controller. El slug
 * es el mismo "id" que ya usan las landing cards (landing-cards-{es,pt}.json)
 * — un slug sin entrada en pages-{lang}.json devuelve 404 real.
 */
@Controller
public class PageController {

    private final LocaleResolver localeResolver;

    public PageController(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @GetMapping(Routes.PAGE_BY_SLUG)
    public String showPage(@PathVariable String slug, Model model, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        Map<String, Object> page = PageContentLoader.loadPage(slug, locale.getLanguage());
        if (page == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Página no encontrada: " + slug);
        }
        model.addAttribute("page", page);
        return "public/page-content";
    }
}
