package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MvpRegressionTest {
    @TempDir
    Path images;

    private User organization() {
        User org = new User();
        org.setId("org");
        org.setRole(Role.ORGANIZATION);
        org.setCity(City.RIVERA);
        org.setPhone("+59899123456");
        org.setProfileCompleted(true);
        org.setAcceptedMaterials(List.of(MaterialCategory.PAPEL));
        return org;
    }

    private User citizen() {
        User user = new User();
        user.setId("citizen");
        user.setUsername("citizen");
        user.setEmail("citizen@example.test");
        user.setPassword("password123");
        user.setRole(Role.USER);
        user.setPhone("+59899123456");
        return user;
    }

    @Test
    void registrationDiscardsPersistenceAndOrganizationFields() {
        UserRepository repo = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded");
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(repo.insert(any(User.class))).thenAnswer(i -> i.getArgument(0));
        User input = citizen();
        input.setProfileCompleted(true);
        input.setAcceptedMaterials(List.of(MaterialCategory.METAL));
        User result = new UserRegistrationService(repo, encoder).registerUser(input, false);
        assertNotSame(input, result);
        assertNull(result.getId());
        assertFalse(result.getProfileCompleted());
        assertTrue(result.getAcceptedMaterials().isEmpty());
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void registrationRejectsSevenCharacterPassword() {
        User input = citizen();
        input.setPassword("1234567");
        assertEquals("error.register.password_min_length", new UserRegistrationService(
                mock(UserRepository.class), mock(PasswordEncoder.class)).validateUserRegistration(input));
    }

    @Test
    void inactiveOrganizationCannotReceiveRequests() {
        User org = organization();
        org.setActive(false);
        UserRepository repo = mock(UserRepository.class);
        when(repo.findById("org")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> new CityOrgService(repo).findOrganizationByIdAndCity("org", City.RIVERA));
    }

    @Test
    void organizationWithoutCompletedProfileCannotReceiveRequests() {
        User org = organization();
        org.setProfileCompleted(false);
        UserRepository repo = mock(UserRepository.class);
        when(repo.findById("org")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> new CityOrgService(repo).findOrganizationByIdAndCity("org", City.RIVERA));
    }

    @Test
    void emptyActiveListMustNotFallBackToInactiveOrganizations() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByRoleAndCityAndActive(Role.ORGANIZATION, City.RIVERA, true)).thenReturn(List.of());
        when(repo.findByRoleAndCity(Role.ORGANIZATION, City.RIVERA)).thenReturn(List.of(organization()));
        assertTrue(new CityOrgService(repo).getOrganizationsByCity(City.RIVERA).isEmpty());
        verify(repo, never()).findByRoleAndCity(any(), any());
    }

    @Test
    void authenticatedRequestRequiresContactPhone() {
        User user = citizen();
        user.setPhone(null);
        assertThrows(IllegalArgumentException.class, () -> new RequestValidator().validateCreate(
                user, City.RIVERA, "Dirección de prueba", List.of(MaterialCategory.PAPEL), null, null, "org"));
    }

    @Test
    void materialsMustBeAcceptedByOrganization() {
        RequestRepository repo = mock(RequestRepository.class);
        CityOrgService cities = mock(CityOrgService.class);
        when(cities.findOrganizationByIdAndCity("org", City.RIVERA)).thenReturn(organization());
        RequestService service = new RequestService(repo, mock(LocalImageService.class), new RequestValidator(), cities);
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(citizen(), City.RIVERA,
                "Dirección de prueba", null, List.of(MaterialCategory.METAL), null, null, "org", null, null));
        verifyNoInteractions(repo);
    }

    @Test
    void invalidImageMustNotPersistRequest() {
        RequestRepository repo = mock(RequestRepository.class);
        when(repo.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));
        CityOrgService cities = mock(CityOrgService.class);
        when(cities.findOrganizationByIdAndCity("org", City.RIVERA)).thenReturn(organization());
        RequestService service = new RequestService(repo, new LocalImageService(images.toString(), repo),
                new RequestValidator(), cities);
        MockMultipartFile file = new MockMultipartFile("imageFile", "invalid.txt", "text/plain", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> service.createRequestWithImage(citizen(), City.RIVERA,
                "Dirección de prueba", null, List.of(MaterialCategory.PAPEL), null, null, "org", null, null, file));
        verify(repo, never()).save(any(Request.class));
    }

    @Test
    void phoneRepresentationsAreCanonical() {
        assertEquals(PhoneNumber.of("+59899123456"), PhoneNumber.of(" +598 99 123 456 "));
    }
}
