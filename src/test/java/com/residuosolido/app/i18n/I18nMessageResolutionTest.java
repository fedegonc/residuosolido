package com.residuosolido.app.i18n;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "spring.data.mongodb.auto-index-creation=false"
})
class I18nMessageResolutionTest {

    @Autowired
    private MessageSource messageSource;

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "es|register.error.password_min_length|8 caracteres",
            "pt|register.error.password_min_length|8 caracteres",
            "es|error.image.invalid_type|Tipo de archivo no permitido",
            "pt|error.image.invalid_type|Tipo de arquivo não permitido",
            "es|error.image.invalid_extension|Extensión de archivo no permitida",
            "pt|error.image.invalid_extension|Extensão de arquivo não permitida",
            "es|error.image.too_large|tamaño máximo",
            "pt|error.image.too_large|tamanho máximo"
    })
    void userFacingValidationMessage_resolvesToExpectedText(String language, String key, String expectedFragment) {
        Locale locale = new Locale(language);
        String message = messageSource.getMessage(key, null, locale);
        assertTrue(message.toLowerCase(locale).contains(expectedFragment.toLowerCase(locale)),
                "Key '" + key + "' in locale '" + language + "' should contain '" + expectedFragment + "' but was: '" + message + "'");
    }
}
