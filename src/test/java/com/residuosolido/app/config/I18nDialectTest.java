package com.residuosolido.app.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class I18nDialectTest {

    private JsonMessageSource messageSource;
    private TemplateEngine engine;

    @BeforeEach
    void setUp() {
        messageSource = mock(JsonMessageSource.class);
        engine = new TemplateEngine();
        engine.setTemplateResolver(new StringTemplateResolver());
        engine.addDialect(new I18nDialect(messageSource));
    }

    @Test
    void translatesDynamicKeysAfterStandardThymeleafAttributes() {
        when(messageSource.catalogFor(any())).thenReturn(Map.of("nav_register", "Cadastrar-se"));
        Context context = new Context(Locale.forLanguageTag("pt"));
        context.setVariable("key", "nav_register");
        context.setVariable("fallback", "Registrarse");

        String html = engine.process("<span th:attr=\"data-i18n=${key}\" th:text=\"${fallback}\"></span>", context);

        assertTrue(html.contains(">Cadastrar-se</span>"));
        assertFalse(html.contains("Registrarse"));
    }

    @Test
    void escapesTranslatedTextAndAttributeValues() {
        when(messageSource.catalogFor(any())).thenReturn(Map.of("copy", "<script>alert(1)</script> & \"texto\""));

        String html = engine.process("<span data-i18n=\"copy\"></span><input data-i18n-attr=\"placeholder:copy\">", new Context());

        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt; &amp; &quot;texto&quot;"));
        assertTrue(html.contains("placeholder=\"&lt;script&gt;"));
    }

    @Test
    void keepsFallbacksForUnknownKeysAndMalformedAttributeMappings() {
        when(messageSource.catalogFor(any())).thenReturn(Map.of("hint", "Ayuda"));

        String html = engine.process("<span data-i18n=\"missing\">Fallback</span><input data-i18n-attr=\"malformed, title:hint, aria-label:hint, placeholder:missing\" placeholder=\"Original\">", new Context());

        assertTrue(html.contains(">Fallback</span>"));
        assertTrue(html.contains("title=\"Ayuda\""));
        assertTrue(html.contains("aria-label=\"Ayuda\""));
        assertTrue(html.contains("placeholder=\"Original\""));
    }

    @Test
    void preservesSiblingBadgesAndRequiredMarkers() {
        when(messageSource.catalogFor(any())).thenReturn(Map.of("label", "Cidade"));

        String html = engine.process("<label><span data-i18n=\"label\">Ciudad</span><span class=\"req\">*</span></label><a><span data-i18n=\"label\"></span><span class=\"badge\">3</span></a>", new Context());

        assertTrue(html.contains("class=\"req\">*</span>"));
        assertTrue(html.contains("class=\"badge\">3</span>"));
        assertEquals(2, html.split("Cidade", -1).length - 1);
    }
}
