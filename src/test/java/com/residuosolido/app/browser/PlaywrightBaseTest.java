package com.residuosolido.app.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * Base para tests de navegador reales con Playwright.
 *
 * Levanta la app Spring Boot en un puerto real y un navegador Chromium
 * headless directamente en la máquina (sin Docker, sin contenedores).
 * Playwright descarga su propio binario de Chromium la primera vez.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.data.mongodb.uri=mongodb://172.17.0.2:27017/testdb-browser",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=true"
    }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(BrowserTestSeed.class)
public abstract class PlaywrightBaseTest {

    @LocalServerPort
    protected int port;

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected String baseUrl;

    @BeforeAll
    void startBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    void stopBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    /** Login con usuario y password. Asume que está en cualquier página. */
    protected void login(String username, String password) {
        page.navigate(baseUrl + "/auth/login");
        page.locator("#username").fill(username);
        page.locator("input[name='password']").fill(password);
        page.locator("button[type='submit']").click();
        page.waitForURL(url -> !url.contains("/auth/login"), new Page.WaitForURLOptions().setTimeout(10000));
    }

    /** Llenar formulario de solicitud (para ciudadano logueado). */
    protected void fillRequestForm(String city, String address, String material) {
        page.locator("#city").selectOption(city);
        // Esperar a que el JS cargue las organizaciones via fetch
        page.waitForTimeout(2000);
        page.locator("#organizationId option:not([value=''])").first().waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(10000));
        page.locator("#organizationId").selectOption(new SelectOption().setIndex(0));
        page.locator("#address").fill(address);
        page.check("input[name='materials'][value='" + material + "']");
    }

    /** Llenar formulario de solicitud como invitado. */
    protected void fillGuestRequestForm(String guestName, String countryCode,
                                         String phoneNational, String city,
                                         String address, String material) {
        page.locator("#guestName").fill(guestName);
        page.locator("#guestPhoneCountryCode").selectOption(countryCode);
        page.locator("#guestPhoneNational").fill(phoneNational);
        page.locator("#city").selectOption(city);
        // Esperar a que el JS cargue las organizaciones via fetch
        page.waitForTimeout(2000);
        page.locator("#organizationId option:not([value=''])").first().waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(10000));
        page.locator("#organizationId").selectOption(new SelectOption().setIndex(0));
        page.locator("#address").fill(address);
        page.check("input[name='materials'][value='" + material + "']");
    }
}
