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
        page.navigate(baseUrl + "/registrarse");
        page.locator("[data-i18n='auth_register_title']").waitFor();

        page.locator("#username").fill("testuser" + System.currentTimeMillis());
        page.locator("#phoneNational").fill("99123456");
        fillPin("password", "1234");
        page.locator("button[type='submit']").click();

        // El registro auto-loguea y redirige al home del rol (ciudadano → mis solicitudes)
        page.waitForURL(url -> !url.contains("/registrarse"));
        assertTrue(page.url().contains("/mis-solicitudes"),
                "Después de registrar ciudadano debe auto-loguear e ir a /mis-solicitudes");
    }

    @Test
    @DisplayName("#15 Registro de organización → onboarding")
    void registerOrganization() {
        page.navigate(baseUrl + "/registrarse-organizacion");
        page.locator("[data-i18n='auth_register_org_title']").waitFor();

        page.locator("#username").fill("testorg" + System.currentTimeMillis());
        page.locator("#tipo").selectOption("CENTRO_ACOPIO");
        page.locator("#phoneNational").fill("99123457");
        fillPin("password", "1234");
        page.locator("button[type='submit']").click();

        // El registro auto-loguea y redirige al home del rol (org → /acopio)
        page.waitForURL(url -> !url.contains("/registrarse"));
        assertTrue(page.url().contains("/acopio") || page.url().contains("/mi-organizacion"),
                "Después de registrar org debe auto-loguear e ir a /acopio");
    }

    @Test
    @DisplayName("#16 Cambio de idioma es → pt")
    void switchLanguageToPortuguese() {
        page.navigate(baseUrl + "/");
        page.locator("h1.hero__title").waitFor();

        // Capturar texto en español
        String titleEs = page.locator("h1.hero__title").innerText();

        // Clickear botón PT (selector correcto: .navbar__lang-btn con href ?lang=pt)
        page.locator("a.navbar__lang-btn[href*='lang=pt']").first().click();
        page.waitForLoadState();

        // Verificar que el texto cambió
        String titlePt = page.locator("h1.hero__title").innerText();
        assertTrue(!titleEs.equals(titlePt),
                "El texto debe cambiar al switchear a portugués");
    }

    @Test
    @DisplayName("#17 Instalación de PWA")
    void pwaInstallButton() {
        page.navigate(baseUrl + "/");
        page.locator("h1.hero__title").waitFor();

        // Verificar que el botón de instalar app existe (aunque esté hidden inicialmente)
        Locator installBtn = page.locator("[data-install-app]");
        assertTrue(installBtn.count() > 0,
                "Debe existir botón de instalación PWA");
    }

    @Test
    @DisplayName("#18 Navegación mobile con menú hamburguesa")
    void mobileNavigation() {
        // Redimensionar a viewport mobile
        page.setViewportSize(375, 812);
        page.navigate(baseUrl + "/");
        page.locator("h1.hero__title").waitFor();

        // Verificar que el menú hamburguesa (.navbar__toggle) existe
        Locator menuToggle = page.locator(".navbar__toggle");
        assertTrue(menuToggle.isVisible(), "Debe haber hamburguesa en mobile");

        // Clickear para abrir
        menuToggle.click();
        page.waitForLoadState();

        // Verificar que el menú dropdown se abrió
        Locator dropdown = page.locator(".dropdown");
        assertTrue(dropdown.isVisible(),
                "El dropdown debe abrirse al clickear hamburguesa");
    }

    @Test
    @DisplayName("#20 Cross-role: ciudadano no accede a /acopio")
    void crossRoleDenied() {
        login("juan", "5678");

        // Intentar acceder a área de organización
        page.navigate(baseUrl + "/acopio/solicitudes");

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
