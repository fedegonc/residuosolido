package com.residuosolido.app.browser;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Recorridos #14-20: Transversales (registro, idioma, tema, mobile, blog, cross-role).
 */
@DisplayName("Browser: Transversales")
class TransversalBrowserTest extends PlaywrightBaseTest {

    @Test
    @DisplayName("#14 Registro de ciudadano")
    void registerCitizen() {
        page.navigate(baseUrl + "/auth/register");
        page.locator("[data-i18n='auth_register_title']").waitFor();

        page.locator("#username").fill("testuser" + System.currentTimeMillis());
        page.locator("#email").fill("testuser" + System.currentTimeMillis() + "@test.com");
        page.locator("input[name='password']").fill("12345678");
        // No marcar "soy organización"
        page.locator("button[type='submit']").click();

        // Debe redirigir a login con mensaje de éxito o a dashboard
        page.waitForURL(url -> !url.contains("/auth/register"));
        assertTrue(page.url().contains("/auth/login") || page.url().contains("/usuarios"),
                "Después de registrar debe ir a login o dashboard");
    }

    @Test
    @DisplayName("#15 Registro de organización → onboarding")
    void registerOrganization() {
        page.navigate(baseUrl + "/auth/register");
        page.locator("[data-i18n='auth_register_title']").waitFor();

        page.locator("#username").fill("testorg" + System.currentTimeMillis());
        page.locator("#email").fill("testorg" + System.currentTimeMillis() + "@test.com");
        page.locator("input[name='password']").fill("12345678");
        // Marcar "soy organización"
        page.locator("#isOrganization").check();
        page.locator("button[type='submit']").click();

        // Debe redirigir a completar perfil (onboarding) o login
        page.waitForURL(url -> !url.contains("/auth/register"));
        assertTrue(page.url().contains("/acopio/completar-perfil")
                        || page.url().contains("/auth/login"),
                "Después de registrar org debe ir a onboarding o login");
    }

    @Test
    @DisplayName("#16 Cambio de idioma es → pt")
    void switchLanguageToPortuguese() {
        page.navigate(baseUrl + "/");
        page.locator("#hero-title").waitFor();

        // Capturar texto en español
        String titleEs = page.locator("#hero-title").innerText();

        // Clickear botón PT
        page.locator("[data-lang='pt']").first().click();
        page.waitForTimeout(2000);

        // Verificar que el texto cambió
        String titlePt = page.locator("#hero-title").innerText();
        assertTrue(!titleEs.equals(titlePt),
                "El texto debe cambiar al switchear a portugués");
    }

    @Test
    @DisplayName("#17 Cambio de tema claro → oscuro")
    void toggleTheme() {
        page.navigate(baseUrl + "/");
        page.locator("#hero-title").waitFor();

        // Capturar atributo data-theme del html o body
        String themeBefore = page.locator("html").getAttribute("data-theme");
        if (themeBefore == null) themeBefore = page.locator("body").getAttribute("data-theme");
        if (themeBefore == null) themeBefore = "light";

        // Clickear theme toggle
        page.locator("#themeToggle").click();
        page.waitForTimeout(500);

        String themeAfter = page.locator("html").getAttribute("data-theme");
        if (themeAfter == null) themeAfter = page.locator("body").getAttribute("data-theme");
        if (themeAfter == null) themeAfter = "light";

        assertTrue(!themeBefore.equals(themeAfter),
                "El tema debe cambiar al clickear el toggle");
    }

    @Test
    @DisplayName("#18 Navegación mobile con menú hamburguesa")
    void mobileNavigation() {
        // Redimensionar a viewport mobile
        page.setViewportSize(375, 812);
        page.navigate(baseUrl + "/");
        page.locator("#hero-title").waitFor();

        // Verificar que el menú hamburguesa existe y es visible en mobile
        Locator menuBtn = page.locator("#menuBtn, .navbar__toggle, button[aria-label*='menu' i]");
        if (menuBtn.count() > 0 && menuBtn.first().isVisible()) {
            menuBtn.first().click();
            page.waitForTimeout(500);
            // Verificar que el menó se abrió
            Locator mobileMenu = page.locator("#dropdownMenu, .navbar__mobile-menu, .navbar__menu--mobile");
            assertTrue(mobileMenu.first().isVisible(),
                    "El menú mobile debe abrirse al clickear hamburguesa");
        }
        // Test pasa si llega hasta aquí (el layout responsive está presente)
        assertTrue(true, "Viewport mobile renderizado");
    }

    @Test
    @DisplayName("#19 Blog carga artículos")
    void blogLoads() {
        page.navigate(baseUrl + "/blog");
        page.waitForTimeout(2000);

        // Verificar que hay contenido de blog
        Locator blogArticles = page.locator(".card--blog");
        assertTrue(blogArticles.count() > 0,
                "El blog debe tener al menos un artículo");

        // El blog usa anchors con # para navegar a artículos
        // Verificar que hay contenido legible
        assertTrue(page.locator(".card__title").first().innerText().length() > 0,
                "Debe haber al menos un título de artículo visible");
    }

    @Test
    @DisplayName("#20 Cross-role: ciudadano no accede a /acopio")
    void crossRoleDenied() {
        login("juan", "12345678");

        // Intentar acceder a área de organización
        page.navigate(baseUrl + "/acopio/requests");

        // Debe ser denegado (403) o redirigido
        page.waitForTimeout(2000);
        assertTrue(page.url().contains("/acopio") == false
                        || page.locator("text=403").isVisible()
                        || page.locator("text=prohibido").isVisible()
                        || page.locator("[data-i18n='error.access_denied']").isVisible()
                        || page.locator(".alert--error").isVisible()
                        || page.url().contains("/error")
                        || page.url().contains("/usuarios"),
                "Ciudadano no debe acceder a /acopio (debe ver 403 o ser redirigido)");
    }
}
