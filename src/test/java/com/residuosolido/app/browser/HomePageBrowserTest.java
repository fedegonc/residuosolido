package com.residuosolido.app.browser;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de navegador real con Playwright (Chromium headless).
 *
 * Verifica que la app renderiza en un navegador real y que los flujos
 * básicos funcionan end-to-end. Estos tests suben la cobertura de
 * controllers porque ejecutan el stack completo (Spring Security +
 * Thymeleaf + MongoDB), no solo MockMvc.
 */
@DisplayName("Browser: flujos de navegador real (Playwright)")
class HomePageBrowserTest extends PlaywrightBaseTest {

    @Test
    @DisplayName("Home page renderiza con título y badge de ciudad")
    void homePageLoads() {
        page.navigate(baseUrl + "/");

        Locator title = page.locator("#hero-title");
        title.waitFor();
        assertTrue(title.innerText().contains("reciclables"),
                "El título debe mencionar reciclables");

        Locator badge = page.locator("[data-i18n='badge']");
        assertTrue(badge.innerText().contains("Rivera"),
                "El badge debe mostrar Rivera");
    }

    @Test
    @DisplayName("Navegación: home → login → registro")
    void navigationHomeToLoginToRegister() {
        page.navigate(baseUrl + "/");

        page.locator("a[href*='/auth/login']").first().click();
        page.locator("[data-i18n='auth_login_title']").waitFor();
        assertTrue(page.locator("[data-i18n='auth_login_title']").innerText().contains("sesi"),
                "Debe mostrar el título de login");

        page.locator("a[href*='/auth/register']").first().click();
        page.locator("[data-i18n='auth_register_title']").waitFor();
        assertTrue(page.locator("[data-i18n='auth_register_title']").innerText().contains("cuenta"),
                "Debe mostrar el título de registro");
    }

    @Test
    @DisplayName("Login con credenciales inválidas muestra error")
    void loginWithInvalidCredentialsShowsError() {
        page.navigate(baseUrl + "/auth/login");

        page.locator("#username").fill("usuario_inexistente");
        page.locator("input[name='password']").fill("clave_mala_123");
        page.locator("button[type='submit']").click();

        page.locator(".alert--error").waitFor();
        assertTrue(page.locator(".alert--error").innerText().length() > 0,
                "Debe mostrar un mensaje de error de login");
    }

    @Test
    @DisplayName("Página de rastreo de solicitud carga formulario")
    void trackPageLoads() {
        page.navigate(baseUrl + "/rastrear");

        page.locator("[data-i18n='track_title']").waitFor();
        assertTrue(page.locator("[data-i18n='track_title']").innerText().length() > 0,
                "La página de rastreo debe tener título");

        Locator phoneInput = page.locator("input[name*='phone'], input[id*='phone']").first();
        assertTrue(phoneInput.isVisible(),
                "El campo teléfono debe ser visible");
    }

    @Test
    @DisplayName("Ruta protegida /acopio redirige a login")
    void protectedRouteRedirectsToLogin() {
        page.navigate(baseUrl + "/acopio/requests");

        page.waitForURL("**/auth/login**");
        assertTrue(page.url().contains("/auth/login"),
                "Acceso anónimo a /acopio debe redirigir a login");
    }
}
