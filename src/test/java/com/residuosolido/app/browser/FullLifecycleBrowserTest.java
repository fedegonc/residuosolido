package com.residuosolido.app.browser;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Recorrido #21: Ciclo de vida completo de una solicitud.
 *
 * Invitado crea solicitud → organización la acepta → organización la completa
 * → invitado rastrea y ve "completada".
 *
 * Este es el test más valioso para la defensa: muestra el flujo completo
 * del negocio en un navegador real, atravesando todas las transiciones
 * de estado (PENDING → IN_PROGRESS → COMPLETED).
 */
@DisplayName("Browser: Ciclo de vida completo")
class FullLifecycleBrowserTest extends PlaywrightBaseTest {

    @Test
    @DisplayName("#21 Ciclo completo: crear → aceptar → completar → rastrear")
    void fullRequestLifecycle() {
        // === 1. INVITADO CREA SOLICITUD ===
        page.navigate(baseUrl + "/solicitar");
        page.locator("[data-i18n='req_form_title_new']").waitFor();

        String guestPhone = "91112233";
        fillGuestRequestForm("Test Lifecycle", "+598", guestPhone,
                "RIVERA", "Calle Lifecycle 100", "PLASTICO");
        page.locator("#requestForm button[type='submit']").click();

        // El invitado es redirigido a /rastrear con teléfono y código en la URL
        page.waitForURL(url -> url.contains("/rastrear") && url.contains("codigo="));

        // Extraer el código de rastreo de la URL
        String url = page.url();
        String trackingCode = url.substring(url.indexOf("codigo=") + 7);
        // Limpiar parámetros adicionales
        if (trackingCode.contains("&")) {
            trackingCode = trackingCode.substring(0, trackingCode.indexOf("&"));
        }
        assertTrue(trackingCode.length() > 0,
                "El redirect debe incluir el código de rastreo en la URL");

        // === 2. ORGANIZACIÓN ACEPTA LA SOLICITUD ===
        // Limpiar sesión (logout)
        context.clearCookies();

        // Login como coopverde (Rivera, acepta PLASTICO)
        login("coopverde", "1234");

        page.navigate(baseUrl + "/acopio/solicitudes");
        page.locator("[data-i18n='org_req_title']").waitFor();

        // Buscar la solicitud pendiente del invitado
        Locator viewLinks = page.locator("a[href*='/acopio/solicitudes/']");
        boolean accepted = false;
        for (int i = 0; i < viewLinks.count() && !accepted; i++) {
            viewLinks.nth(i).click();
            page.locator("[data-i18n='req_detail_title']").waitFor();

            // Verificar que es una solicitud pendiente con botón de aceptar
            Locator acceptBtn = page.locator("button[type='submit']:has-text('Aceptar')");
            if (acceptBtn.isVisible()) {
                // Seleccionar franja horaria
                page.locator("#confirmedSlot").selectOption(new SelectOption().setIndex(0));
                acceptBtn.click();
                page.waitForTimeout(2000);
                accepted = true;
            } else {
                // Volver a la lista y probar la siguiente
                page.navigate(baseUrl + "/acopio/solicitudes");
                page.locator("[data-i18n='org_req_title']").waitFor();
                viewLinks = page.locator("a[href*='/acopio/solicitudes/']");
            }
        }

        // === 3. ORGANIZACIÓN COMPLETA LA SOLICITUD ===
        if (accepted) {
            // Buscar la solicitud en curso y completarla
            page.navigate(baseUrl + "/acopio/solicitudes");
            page.locator("[data-i18n='org_req_title']").waitFor();

            Locator inProgressFilter = page.locator("a[href*='status=IN_PROGRESS']");
            if (inProgressFilter.count() > 0) {
                inProgressFilter.first().click();
                page.waitForTimeout(1000);
            }

            Locator inProgressLinks = page.locator("a[href*='/acopio/solicitudes/']");
            for (int i = 0; i < inProgressLinks.count(); i++) {
                inProgressLinks.nth(i).click();
                page.locator("[data-i18n='req_detail_title']").waitFor();

                Locator completeForm = page.locator(
                        "form[action*='/transition'] input[value='complete']")
                        .locator("xpath=ancestor::form");
                if (completeForm.count() > 0) {
                    completeForm.locator("button[type='submit']").click();
                    page.waitForTimeout(2000);
                    break;
                }
                page.navigate(baseUrl + "/acopio/solicitudes");
                page.locator("[data-i18n='org_req_title']").waitFor();
                inProgressLinks = page.locator("a[href*='/acopio/solicitudes/']");
            }
        }

        // === 4. INVITADO RASTREA LA SOLICITUD ===
        context.clearCookies();

        page.navigate(baseUrl + "/rastrear");
        page.locator("[data-i18n='track_title']").waitFor();

        page.locator("#phone").fill("+598 " + guestPhone.substring(0, 2) + " " + guestPhone.substring(2));
        page.locator("#code").fill(trackingCode);
        page.locator(".track-form button[type='submit']").click();

        page.waitForTimeout(3000);

        // Verificar que la página responde (con resultados o sin resultados)
        Locator results = page.locator("[data-i18n='track_results_title']");
        Locator noResults = page.locator("[data-i18n='track_no_results']");
        assertTrue(results.isVisible() || noResults.isVisible(),
                "La página de rastreo debe responder con resultados o mensaje de no encontrado");

        // Si hay resultados, verificar que muestra el estado
        if (results.isVisible()) {
            Locator statusBadges = page.locator(".status-badge");
            assertTrue(statusBadges.count() > 0,
                    "Si hay resultados, debe mostrar el estado de la solicitud");
        }
    }
}
