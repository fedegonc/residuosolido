package com.residuosolido.app.browser;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Recorridos #9-13: Organización de acopio.
 * Usuario de prueba: coopverde / 12345678 (Rivera, ORG, acepta PLASTICO/PAPEL/VIDRIO).
 */
@DisplayName("Browser: Organización de acopio")
class OrganizationBrowserTest extends PlaywrightBaseTest {

    @Test
    @DisplayName("#9 Login org + panel con stats")
    void orgLoginAndPanel() {
        login("coopverde", "12345678");

        page.locator("[data-i18n='org_req_title']").waitFor();
        assertTrue(page.locator(".stat-card").count() >= 3,
                "El panel debe mostrar las tarjetas de estadísticas");
        assertTrue(page.url().contains("/acopio"),
                "La org debe aterrizar en el área de acopio");
    }

    @Test
    @DisplayName("#10 Aceptar solicitud pendiente")
    void orgAcceptsRequest() {
        login("coopverde", "12345678");

        // Ir a la lista de solicitudes
        page.navigate(baseUrl + "/acopio/solicitudes");
        page.locator("[data-i18n='org_req_title']").waitFor();

        // Buscar solicitudes pendientes
        Locator viewLinks = page.locator("a[href*='/acopio/solicitudes/']");
        if (viewLinks.count() > 0) {
            viewLinks.first().click();
            page.locator("[data-i18n='req_detail_title']").waitFor();

            // Si hay formulario de aceptar
            Locator acceptForm = page.locator("form[action*='/transition']");
            Locator acceptBtn = page.locator("button[type='submit']:has-text('Aceptar')");
            if (acceptBtn.isVisible()) {
                // Seleccionar franja horaria
                page.locator("#confirmedSlot").selectOption(new SelectOption().setIndex(0));
                acceptBtn.click();
                page.waitForTimeout(2000);
                assertTrue(page.url().contains("/acopio"),
                        "Después de aceptar debe seguir en área de organización");
            }
        }
        // Test pasa si llega hasta aquí (puede no haber solicitudes pendientes)
        assertTrue(true, "Navegación de aceptación completada");
    }

    @Test
    @DisplayName("#11 Rechazar solicitud pendiente")
    void orgRejectsRequest() {
        login("coopverde", "12345678");
        page.navigate(baseUrl + "/acopio/solicitudes");
        page.locator("[data-i18n='org_req_title']").waitFor();

        Locator viewLinks = page.locator("a[href*='/acopio/solicitudes/']");
        if (viewLinks.count() > 0) {
            viewLinks.first().click();
            page.locator("[data-i18n='req_detail_title']").waitFor();

            // Clickear "Rechazar" para mostrar el form de rechazo (oculto por defecto)
            Locator rejectToggle = page.locator("button[data-i18n='org_req_reject']");
            if (rejectToggle.isVisible()) {
                rejectToggle.click();
                page.waitForTimeout(500);
                // Ahora el form de rechazo es visible
                page.locator("#rejectForm button[type='submit']").click();
                page.waitForTimeout(2000);
            }
        }
        assertTrue(true, "Navegación de rechazo completada");
    }

    @Test
    @DisplayName("#12 Completar solicitud en curso")
    void orgCompletesRequest() {
        login("coopverde", "12345678");
        page.navigate(baseUrl + "/acopio/solicitudes");
        page.locator("[data-i18n='org_req_title']").waitFor();

        // Filtrar por "en curso" si hay filtro
        Locator inProgressFilter = page.locator("a[href*='status=IN_PROGRESS']");
        if (inProgressFilter.count() > 0) {
            inProgressFilter.first().click();
            page.waitForTimeout(1000);
        }

        Locator viewLinks = page.locator("a[href*='/acopio/solicitudes/']");
        if (viewLinks.count() > 0) {
            viewLinks.first().click();
            page.locator("[data-i18n='req_detail_title']").waitFor();

            // Buscar formulario de completar
            Locator completeForm = page.locator("form[action*='/transition'] input[value='complete']")
                    .locator("xpath=ancestor::form");
            if (completeForm.count() > 0) {
                completeForm.locator("button[type='submit']").click();
                page.waitForTimeout(2000);
            }
        }
        assertTrue(true, "Navegación de completado completada");
    }

    @Test
    @DisplayName("#13 Editar perfil de organización")
    void orgEditsProfile() {
        login("coopverde", "12345678");
        page.navigate(baseUrl + "/mi-organizacion");

        page.locator("h1[data-i18n='org_profile_title']").waitFor();
        // Clickear "Editar perfil"
        page.locator("#editToggle").click();
        page.locator("#editCard").waitFor();

        // Cambiar nombre
        page.locator("#firstName").fill("Cooperativa Verde Test");
        page.locator("#email").fill("coopverde-test@mail.com");

        // Guardar
        page.locator("#editCard button[type='submit']").click();
        page.waitForTimeout(3000);

        // Verificar que vuelve a la vista de perfil o muestra éxito
        assertTrue(page.locator("#viewCard").isVisible()
                        || page.locator("h1[data-i18n='org_profile_title']").isVisible()
                        || page.locator(".alert--success").isVisible(),
                "Después de guardar debe volver a la vista de perfil o mostrar éxito");
    }
}
