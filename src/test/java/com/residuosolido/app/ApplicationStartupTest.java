package com.residuosolido.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test para verificar que la aplicación arranca correctamente
 * después de la refactorización de controladores admin
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Test
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se carga correctamente
        // Si hay problemas de configuración, inyección de dependencias o 
        // controladores mal configurados, este test fallará
    }
    
    @Test
    void applicationStarts() {
        // Test adicional para verificar que la aplicación puede arrancar
        // sin errores después de la refactorización SOLID
        // La anotación @SpringBootTest ya carga todo el contexto
        assert true; // Si llegamos aquí, la aplicación arrancó correctamente
    }
}
