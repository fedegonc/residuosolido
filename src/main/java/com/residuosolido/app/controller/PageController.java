package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.util.LandingCardLoader;
import com.residuosolido.app.util.PageContentLoader;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    /** Página de inicio pública (landing page). */
    @GetMapping({Routes.HOME, Routes.INDEX})
    public String rootOrIndex(Model model, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        model.addAttribute("cards", LandingCardLoader.loadCards(locale.getLanguage()));
        return "public/index";
    }

    @GetMapping(Routes.PAGE_BY_SLUG)
    public String showPage(@PathVariable String slug, Model model, HttpServletRequest request) {
        Locale locale = localeResolver.resolveLocale(request);
        Map<String, Object> page = PageContentLoader.loadPage(slug, locale.getLanguage())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Página no encontrada: " + slug));
        model.addAttribute("page", page);
        return "public/page-content";
    }

    /**
     * QR de la landing: PNG con el origin del request, para compartir la página
     * mostrando el celular. Funciona igual en localhost, LAN y Render (con
     * forward-headers-strategy el scheme/host ya vienen resueltos del proxy).
     */
    @GetMapping(value = Routes.QR, produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> qr(HttpServletRequest request) throws Exception {
        String origin = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() == 80 || request.getServerPort() == 443
                   ? "" : ":" + request.getServerPort());
        BitMatrix matrix = new QRCodeWriter().encode(origin + "/", BarcodeFormat.QR_CODE,
                320, 320, Map.of(EncodeHintType.MARGIN, 1));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(out.toByteArray());
    }
}
