package com.residuosolido.app.service;

import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Tests de borde de precondiciones — OrganizationService era un archivo
 * nuevo sin guards en 3 de sus 5 métodos públicos (createForUser,
 * findByUserId, findById), encontrado en la auditoría de "mago del parche
 * vs. senior" (misma clase de gap ya corregida en CityOrgService,
 * RequestMetricsService y UserService en esta misma sesión).
 */
@Tag("unit")
class OrganizationServiceTest {

    private OrganizationRepository organizationRepository;
    private UserRepository userRepository;
    private OrganizationService service;

    @BeforeEach
    void setUp() {
        organizationRepository = mock(OrganizationRepository.class);
        userRepository = mock(UserRepository.class);
        service = new OrganizationService(organizationRepository, userRepository);
    }

    @Test
    void createForUser_nullUser_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.createForUser(null));
    }

    @Test
    void findByUserId_null_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.findByUserId(null));
    }

    @Test
    void findByUserId_blank_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.findByUserId("   "));
    }

    @Test
    void findById_null_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.findById(null));
    }

    @Test
    void findByUser_nullUser_throwsValidation() {
        assertThrows(ValidationException.class, () -> service.findByUser((User) null));
    }
}
