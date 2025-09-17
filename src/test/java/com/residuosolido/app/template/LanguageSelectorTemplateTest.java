package com.residuosolido.app.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para verificar que el selector de idioma
 * está presente en el template index.html
 */
class LanguageSelectorTemplateTest {

    private static final String TEMPLATE_PATH = "src/main/resources/templates/index.html";

    @Test
    @DisplayName("El template index.html debe contener el selector de idioma")
    void indexTemplateShouldContainLanguageSelector() throws IOException {
        // Leer el contenido del archivo HTML
        Path templatePath = Paths.get(TEMPLATE_PATH);
        assertTrue(Files.exists(templatePath), "El archivo index.html debe existir");
        
        String htmlContent = Files.readString(templatePath);
        
        // Verificar que contiene el selector de idioma
        assertTrue(htmlContent.contains("id=\"language-selector\""), 
                "El HTML debe contener un elemento con id='language-selector'");
    }

    @Test
    @DisplayName("El selector de idioma debe tener las opciones correctas")
    void languageSelectorShouldHaveCorrectOptions() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar opciones de idioma
        assertTrue(htmlContent.contains("value=\"es\""), 
                "Debe contener la opción de español");
        assertTrue(htmlContent.contains("🇪🇸 Español"), 
                "Debe mostrar 'Español' con bandera");
        assertTrue(htmlContent.contains("value=\"pt\""), 
                "Debe contener la opción de portugués");
        assertTrue(htmlContent.contains("🇧🇷 Português"), 
                "Debe mostrar 'Português' con bandera");
    }

    @Test
    @DisplayName("El selector de idioma NO debe contener inglés")
    void languageSelectorShouldNotContainEnglish() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar que NO contiene inglés
        assertFalse(htmlContent.contains("value=\"en\""), 
                "NO debe contener la opción de inglés");
        assertFalse(htmlContent.contains("English"), 
                "NO debe mostrar 'English'");
    }

    @Test
    @DisplayName("El selector debe estar en el navbar")
    void languageSelectorShouldBeInNavbar() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Buscar el navbar y verificar que contiene el selector
        int navbarStart = htmlContent.indexOf("<nav");
        int navbarEnd = htmlContent.indexOf("</nav>") + 6;
        assertTrue(navbarStart != -1 && navbarEnd != -1, "Debe existir un elemento nav");
        
        String navbarContent = htmlContent.substring(navbarStart, navbarEnd);
        assertTrue(navbarContent.contains("language-selector"), 
                "El selector de idioma debe estar dentro del navbar");
    }

    @Test
    @DisplayName("Debe tener JavaScript para manejar el cambio de idioma")
    void shouldHaveLanguageChangeJavaScript() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar que existe el JavaScript para manejar el selector
        assertTrue(htmlContent.contains("getElementById('language-selector')"), 
                "Debe contener JavaScript que acceda al selector de idioma");
        assertTrue(htmlContent.contains("addEventListener('change'"), 
                "Debe tener un event listener para el cambio");
        assertTrue(htmlContent.contains("localStorage.setItem('preferred-language'"), 
                "Debe guardar la preferencia en localStorage");
    }

    @Test
    @DisplayName("El español debe estar seleccionado por defecto")
    void spanishShouldBeSelectedByDefault() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar que el español tiene el atributo selected
        assertTrue(htmlContent.contains("value=\"es\" selected"), 
                "El español debe estar marcado como seleccionado por defecto");
    }

    @Test
    @DisplayName("Debe tener las clases CSS correctas para el estilo")
    void shouldHaveCorrectCssClasses() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar que tiene las clases de Tailwind CSS
        assertTrue(htmlContent.contains("px-3 py-2 text-sm font-medium"), 
                "Debe tener clases de padding y tipografía");
        assertTrue(htmlContent.contains("border border-gray-300 rounded-md"), 
                "Debe tener clases de borde y esquinas redondeadas");
        assertTrue(htmlContent.contains("focus:ring-green-500"), 
                "Debe tener clases de focus con color verde");
    }

    @Test
    @DisplayName("Debe mostrar mensajes de notificación en ambos idiomas")
    void shouldHaveNotificationMessagesForBothLanguages() throws IOException {
        Path templatePath = Paths.get(TEMPLATE_PATH);
        String htmlContent = Files.readString(templatePath);
        
        // Verificar mensajes de notificación
        assertTrue(htmlContent.contains("Cambiando a Español"), 
                "Debe contener mensaje en español");
        assertTrue(htmlContent.contains("Mudando para Português"), 
                "Debe contener mensaje en portugués");
    }
}
