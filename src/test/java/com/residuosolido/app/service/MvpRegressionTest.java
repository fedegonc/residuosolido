package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
class MvpRegressionTest {
    @TempDir
    Path images;

    private Organization organization() {
        return TestFixtures.organization("org", City.RIVERA, MaterialCategory.PAPEL);
    }

    private User citizen() {
        User user = TestFixtures.citizen("citizen", "+59899123456");
        user.setUsername("citizen");
        user.setPassword("1234");
        return user;
    }

    @Test
    void registrationCreatesOrganizationForOrgRole() {
        UserRepository repo = mock(UserRepository.class);
        OrganizationRepository orgRepo = mock(OrganizationRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded");
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(repo.insert(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(orgRepo.save(any(Organization.class))).thenAnswer(i -> i.getArgument(0));

        User input = citizen();
        User result = new UserRegistrationService(repo, encoder, new OrganizationService(orgRepo))
                .registerOrganization(input, com.residuosolido.app.enums.OrgType.CENTRO_ACOPIO);

        assertNotSame(input, result);
        assertNull(result.getId());
        verify(repo).insert(any(User.class));
        verify(orgRepo).save(any(Organization.class));
    }

    @Test
    void registrationDoesNotCreateOrganizationForCitizen() {
        UserRepository repo = mock(UserRepository.class);
        OrganizationRepository orgRepo = mock(OrganizationRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded");
        when(repo.insert(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = new UserRegistrationService(repo, encoder, new OrganizationService(orgRepo))
                .registerCitizen(citizen());

        assertNotSame(citizen(), result);
        verify(repo).insert(any(User.class));
        verify(orgRepo, never()).save(any());
    }

    @Test
    void registrationRejectsInvalidPin() {
        User input = citizen();
        input.setPassword("12");
        assertEquals(com.residuosolido.app.exception.ServerMessage.ERROR_REGISTER_PIN_INVALID,
                new UserRegistrationService(mock(UserRepository.class), mock(PasswordEncoder.class),
                        mock(OrganizationService.class)).validateUserRegistration(input));
    }

    @Test
    void organizationWithoutCompletedProfileCannotReceiveRequests() {
        Organization org = organization();
        org.setProfileCompleted(false);
        OrganizationRepository repo = mock(OrganizationRepository.class);
        when(repo.findById("org")).thenReturn(Optional.of(org));
        assertThrows(IllegalArgumentException.class,
                () -> new CityOrgService(repo).findOrganizationByIdAndCity("org", City.RIVERA));
    }

    @Test
    void emptyOrgListReturnsEmpty() {
        OrganizationRepository repo = mock(OrganizationRepository.class);
        when(repo.findByCity(City.RIVERA)).thenReturn(List.of());
        assertTrue(new CityOrgService(repo).getOrganizationsByCity(City.RIVERA).isEmpty());
    }

    @Test
    void authenticatedRequestRequiresContactPhone() {
        User user = citizen();
        user.setPhone(null);
        RequestValidator validator = new RequestValidator(mock(OrganizationRepository.class));
        assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(
                user, City.RIVERA, "Dirección de prueba", List.of(MaterialCategory.PAPEL), "org"));
    }

    @Test
    void materialsMustBeAcceptedByOrganization() {
        RequestRepository repo = mock(RequestRepository.class);
        CityOrgService cities = mock(CityOrgService.class);
        when(cities.findOrganizationByIdAndCity("org", City.RIVERA)).thenReturn(organization());
        RequestService service = new RequestService(repo, mock(LocalImageService.class), cities,
                mock(ApplicationEventPublisher.class), new RequestValidator(mock(OrganizationRepository.class)));
        assertThrows(IllegalArgumentException.class, () -> service.createRequest(citizen(), City.RIVERA,
                "Dirección de prueba", null, List.of(MaterialCategory.METAL), "org"));
        verifyNoInteractions(repo);
    }

    @Test
    void invalidImageMustNotPersistRequest() {
        RequestRepository repo = mock(RequestRepository.class);
        when(repo.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));
        CityOrgService cities = mock(CityOrgService.class);
        when(cities.findOrganizationByIdAndCity("org", City.RIVERA)).thenReturn(organization());
        RequestService service = new RequestService(repo, new LocalImageService(images.toString(), repo),
                cities, mock(ApplicationEventPublisher.class), new RequestValidator(mock(OrganizationRepository.class)));
        MockMultipartFile file = new MockMultipartFile("imageFile", "invalid.txt", "text/plain", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> service.createRequestWithImage(citizen(), City.RIVERA,
                "Dirección de prueba", null, List.of(MaterialCategory.PAPEL), "org", file));
        verify(repo, never()).save(any(Request.class));
    }

    @Test
    void phoneRepresentationsAreCanonical() {
        assertEquals(PhoneNumber.normalize("+59899123456"), PhoneNumber.normalize(" +598 99 123 456 "));
    }
}
