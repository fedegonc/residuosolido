package com.residuosolido.app.browser;

import com.residuosolido.app.config.JsonMessageSource;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.ServiceWorkerPolicy;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutStabilityBrowserTest extends PlaywrightBaseTest {

    @Autowired
    private JsonMessageSource messageSource;

    @Override
    @BeforeEach
    protected void setUp() {
        context = browser.newContext(new Browser.NewContextOptions().setServiceWorkers(ServiceWorkerPolicy.BLOCK));
        page = context.newPage();
        baseUrl = "http://localhost:" + port;
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "pt"})
    void publicPagesAreTranslatedBeforeClientScript(String language) {
        page.route("**/js/app.js", route -> route.abort());
        for (String path : List.of("/", "/entrar", "/registrarse", "/registrarse-organizacion", "/pagina/catadores")) {
            page.navigate(baseUrl + path + "?lang=" + language,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            assertTranslations(language);
        }
        String options = page.request().get(baseUrl + "/solicitudes/org-options?ciudad=RIVERA&lang=" + language).text();
        String label = messageSource.catalogFor(Locale.forLanguageTag(language)).get("req_form_org_select");
        assertTrue(options.contains(">" + label + "</option>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "pt"})
    void protectedPagesAreTranslatedBeforeClientScript(String language) {
        login("juan", "5678");
        page.route("**/js/app.js", route -> route.abort());
        for (String path : List.of("/mis-solicitudes", "/solicitar", "/notificaciones")) {
            page.navigate(baseUrl + path + "?lang=" + language,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            assertTranslations(language);
        }
        page.unroute("**/js/app.js");
        context.clearCookies();
        login("coopverde", "2468");
        page.route("**/js/app.js", route -> route.abort());
        for (String path : List.of("/acopio/solicitudes", "/mi-organizacion")) {
            page.navigate(baseUrl + path + "?lang=" + language,
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            assertTranslations(language);
        }
    }

    @Test
    void navbarDoesNotOverlapOrOverflowInEitherLanguage() {
        for (String language : List.of("es", "pt")) {
            for (int width : List.of(320, 375, 768, 1025, 1280)) {
                page.setViewportSize(width, 900);
                page.navigate(baseUrl + "/?lang=" + language);
                assertNavbarGeometry(language + " / " + width);
            }
        }
        login("juan", "5678");
        for (String language : List.of("es", "pt")) {
            for (int width : List.of(320, 375, 768, 1025, 1280)) {
                page.setViewportSize(width, 900);
                page.navigate(baseUrl + "/mis-solicitudes?lang=" + language);
                assertNavbarGeometry(language + " / ciudadano / " + width);
                assertEquals(4, page.locator(".kanban-column").count());
                if (width == 768) {
                    page.locator(".kanban-board").evaluate("el => { el.scrollLeft = el.scrollWidth; }");
                    page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
                    assertTrue((Boolean) page.locator(".kanban-board").evaluate("el => { const board = el.getBoundingClientRect(); const last = el.lastElementChild.getBoundingClientRect(); return last.left >= board.left - 1 && last.right <= board.right + 1; }"));
                }
            }
        }
    }

    @Test
    void installationPromptDoesNotMoveNavbarControls() {
        page.navigate(baseUrl + "/");
        page.evaluate("() => document.fonts.ready");
        Object before = page.locator(".navbar__lang").evaluate("el => ({x: el.getBoundingClientRect().x, width: el.getBoundingClientRect().width})");
        page.evaluate("() => window.dispatchEvent(new Event('beforeinstallprompt', {cancelable: true}))");
        Object after = page.locator(".navbar__lang").evaluate("el => ({x: el.getBoundingClientRect().x, width: el.getBoundingClientRect().width})");
        assertEquals(before, after);
    }

    @Test
    void loadingClientScriptDoesNotChangeLandingGeometry() {
        page.route("**/js/app.js", route -> route.abort());
        page.navigate(baseUrl + "/?lang=pt");
        page.evaluate("() => document.fonts.ready");
        Object before = landingGeometry();
        page.unroute("**/js/app.js");
        page.addScriptTag(new Page.AddScriptTagOptions().setUrl(baseUrl + "/js/app.js"));
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        assertEquals(before, landingGeometry());
    }

    @Test
    void lateFontDoesNotReflowTheVisibleLanding() {
        AtomicReference<Route> font = new AtomicReference<>();
        page.route("**/*.woff2", font::set);
        page.navigate(baseUrl + "/?lang=pt",
                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForTimeout(350);
        Object before = landingGeometry();
        assertTrue(font.get() != null);
        font.get().resume();
        page.evaluate("() => document.fonts.ready");
        assertEquals(before, landingGeometry());
    }

    private Object landingGeometry() {
        return page.locator(".hero, .qr-card, .hero__cta").evaluateAll("els => els.map(el => {const r = el.getBoundingClientRect(); return {x: r.x, y: r.y, width: r.width, height: r.height};})");
    }

    private void assertTranslations(String language) {
        Map<String, String> copies = messageSource.catalogFor(Locale.forLanguageTag(language));
        Object mismatches = page.locator("[data-i18n], [data-i18n-attr]").evaluateAll("""
                (els, copies) => els.flatMap(el => {
                    const errors = [];
                    const key = el.getAttribute('data-i18n');
                    if (key && el.textContent.trim() !== copies[key]) {
                        errors.push({key, actual: el.textContent.trim(), expected: copies[key] || 'missing'});
                    }
                    const attrs = el.getAttribute('data-i18n-attr');
                    if (attrs) for (const pair of attrs.split(',')) {
                        const [name, attrKey] = pair.trim().split(':');
                        if (el.getAttribute(name) !== copies[attrKey]) errors.push({attribute: name, key: attrKey});
                    }
                    return errors;
                })
                """, copies);
        assertEquals(List.of(), mismatches, page.url());
        page.evaluate("() => document.fonts.ready");
        Object before = page.locator(".hero, .qr-card, .card, .field, .kanban-column").evaluateAll("els => els.map(el => {const r = el.getBoundingClientRect(); return {x: r.x, y: r.y, width: r.width, height: r.height};})");
        page.unroute("**/js/app.js");
        page.addScriptTag(new Page.AddScriptTagOptions().setUrl(baseUrl + "/js/app.js"));
        page.evaluate("() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)))");
        Object after = page.locator(".hero, .qr-card, .card, .field, .kanban-column").evaluateAll("els => els.map(el => {const r = el.getBoundingClientRect(); return {x: r.x, y: r.y, width: r.width, height: r.height};})");
        assertEquals(before, after, page.url());
        page.route("**/js/app.js", route -> route.abort());
    }

    private void assertNavbarGeometry(String context) {
        Object errors = page.locator(".navbar__inner").evaluate("""
                nav => {
                    const parts = [...nav.children].filter(el => el.getBoundingClientRect().width > 0);
                    const bounds = nav.getBoundingClientRect();
                    const errors = [];
                    for (let i = 0; i < parts.length; i++) {
                        const r = parts[i].getBoundingClientRect();
                        if (r.left < bounds.left - 1 || r.right > bounds.right + 1 || r.bottom > bounds.bottom + 1) errors.push(parts[i].className + ' overflow');
                        if (i && parts[i - 1].getBoundingClientRect().right > r.left + 1) errors.push(parts[i].className + ' overlap');
                    }
                    const brand = nav.querySelector('.navbar__brand').getBoundingClientRect();
                    const actions = nav.querySelector('.navbar__actions').getBoundingClientRect();
                    for (const link of nav.querySelectorAll('.navbar__link')) {
                        const r = link.getBoundingClientRect();
                        if (r.width > 0 && (r.left < brand.right || r.right > actions.left)) errors.push('link overlap');
                    }
                    if (document.documentElement.scrollWidth > window.innerWidth + 1) {
                        errors.push('page overflow: ' + document.documentElement.scrollWidth);
                        for (const el of document.querySelectorAll('body, .app-main, .page, .card, .kanban-board, .edu-links, .footer')) {
                            const r = el.getBoundingClientRect();
                            if (r.right > window.innerWidth + 1 || r.left < -1) errors.push(el.className + ': ' + r.left + '..' + r.right);
                        }
                    }
                    return errors;
                }
                """);
        assertTrue(((List<?>) errors).isEmpty(), context + ": " + errors);
    }
}
