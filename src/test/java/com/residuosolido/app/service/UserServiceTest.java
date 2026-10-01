package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Tag("unit")
class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;
    private UserRegistrationService userRegistrationService;
    private PasswordEncoder passwordEncoder;
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository);
        organizationService = mock(OrganizationService.class);
        userRegistrationService = new UserRegistrationService(userRepository, passwordEncoder, organizationService);
    }

    // ===== PIN de 4 dígitos en registro =====

    @Test
    void validateUserRegistration_invalidPin_returnsError() {
        User user = new User();
        user.setUsername("nuevo");
        user.setPhone("+59899123456");
        user.setPassword("12");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ServerMessage error = userRegistrationService.validateUserRegistration(user);
        assertEquals(ServerMessage.ERROR_REGISTER_PIN_INVALID, error);
    }

    @Test
    void validateUserRegistration_nullPin_returnsError() {
        User user = new User();
        user.setUsername("nuevo");
        user.setPhone("+59899123456");
        user.setPassword(null);

        ServerMessage error = userRegistrationService.validateUserRegistration(user);
        assertEquals(ServerMessage.ERROR_REGISTER_PIN_INVALID, error);
    }

    @Test
    void validateUserRegistration_validPin_returnsNull() {
        User user = new User();
        user.setUsername("nuevo");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertNull(userRegistrationService.validateUserRegistration(user));
    }

    @Test
    void validateUserRegistration_missingPhone_returnsError() {
        User user = new User();
        user.setUsername("nuevo");
        user.setPassword("1234");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ServerMessage error = userRegistrationService.validateUserRegistration(user);
        assertEquals(ServerMessage.ERROR_REGISTER_PHONE_REQUIRED, error);
    }

    // ===== registro (ciudadano / organización) =====

    @Test
    void registerCitizen_encodesPasswordAndSetsDefaults() {
        User user = new User();
        user.setUsername("newuser");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userRegistrationService.registerCitizen(user);
        assertEquals("encoded", result.getPassword());
        verify(organizationService, never()).createForUser(any(), any());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerOrganization_createsOrgProfileWithTipo() {
        User user = new User();
        user.setUsername("org1");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userRegistrationService.registerOrganization(user, OrgType.COOPERATIVA);
        verify(organizationService).createForUser(result, OrgType.COOPERATIVA);
    }

    @Test
    void registerOrganization_nullTipo_throwsOrgTypeRequired() {
        User user = new User();
        user.setUsername("orgx");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> userRegistrationService.registerOrganization(user, null));
        assertEquals(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED, ex.key());
        verify(userRepository, never()).insert(any(User.class));
        verify(organizationService, never()).createForUser(any(), any());
    }

    // ===== updateProfile: contacto básico del ciudadano =====

    @Test
    void updateProfile_invalidPhoneFormat_throwsPhoneInvalid() {
        User user = new User();
        user.setId("1");
        user.setUsername("citizen");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(user, null, null, "099123456", City.RIVERA));
        assertEquals("error.phone.invalid", ex.getMessage());
    }

    @Test
    void updateProfile_withPhoneAndCity_updatesContact() {
        User user = new User();
        user.setId("1");
        user.setUsername("citizen");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateProfile(user, null, null, "+598 99 123 456", City.RIVERA);

        assertEquals("+59899123456", result.getPhone());
        assertEquals(City.RIVERA, result.getCity());
    }

    @Test
    void updateProfile_withEmail_setsEmailOnUser() {
        User user = new User();
        user.setId("1");
        user.setUsername("citizen");

        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("nueva@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateProfile(user, "nueva@test.com", null, null, null);

        assertEquals("nueva@test.com", result.getEmail());
    }

    // ===== findAuthenticatedUserByUsername =====

    @Test
    void findAuthenticatedUserByUsername_found_returnsUser() {
        User user = new User();
        user.setId("1");
        user.setUsername("citizen");
        when(userRepository.findByUsername("citizen")).thenReturn(Optional.of(user));

        assertEquals(user, userService.findAuthenticatedUserByUsername("citizen"));
    }

    @Test
    void findAuthenticatedUserByUsername_notFound_throwsValidationException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> userService.findAuthenticatedUserByUsername("ghost"));
    }

    // ===== isAnonymous / resolveUser =====

    @Test
    void isAnonymous_nullAuthentication_returnsTrue() {
        assertTrue(userService.isAnonymous(null));
    }

    @Test
    void isAnonymous_anonymousPrincipal_returnsTrue() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        assertTrue(userService.isAnonymous(auth));
    }

    @Test
    void isAnonymous_realPrincipal_returnsFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("citizen");
        assertFalse(userService.isAnonymous(auth));
    }

    @Test
    void resolveUser_anonymous_returnsNull() {
        assertNull(userService.resolveUser(null));
        verifyNoInteractions(userRepository);
    }

    @Test
    void resolveUser_authenticated_returnsUser() {
        User user = new User();
        user.setId("1");
        user.setUsername("citizen");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("citizen");
        when(auth.getName()).thenReturn("citizen");
        when(userRepository.findByUsername("citizen")).thenReturn(Optional.of(user));

        assertEquals(user, userService.resolveUser(auth));
    }

    // ===== updateUser =====

    @Test
    void updateUser_nullUser_throwsValidationException() {
        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> userService.updateUser(null));
    }

    @Test
    void updateUser_notFound_throwsValidationException() {
        User form = new User();
        form.setId("ghost");
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> userService.updateUser(form));
    }

    @Test
    void updateUser_emailUsedByAnotherUser_throwsValidationException() {
        User existing = new User();
        existing.setId("1");
        User otherOwner = new User();
        otherOwner.setId("2");

        User form = new User();
        form.setId("1");
        form.setEmail("nuevo@test.com");

        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("nuevo@test.com")).thenReturn(Optional.of(otherOwner));

        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> userService.updateUser(form));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_emailTakenInRace_throwsValidationException() {
        User existing = new User();
        existing.setId("1");

        User form = new User();
        form.setId("1");
        form.setEmail("mismo@test.com");

        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("mismo@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("email"));

        assertThrows(com.residuosolido.app.exception.ValidationException.class,
                () -> userService.updateUser(form));
    }

    @Test
    void updateUser_sameEmailAsSelf_doesNotThrow() {
        User existing = new User();
        existing.setId("1");
        existing.setEmail("mismo@test.com");

        User form = new User();
        form.setId("1");
        form.setEmail("mismo@test.com");

        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("mismo@test.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(form);
        assertEquals("mismo@test.com", result.getEmail());
    }

    @Test
    void updateUser_nullEmail_skipsUniquenessCheck() {
        User existing = new User();
        existing.setId("1");
        existing.setEmail("previo@test.com");

        User form = new User();
        form.setId("1");

        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(form);
        assertEquals("previo@test.com", result.getEmail());
        verify(userRepository, never()).findByEmailIgnoreCase(any());
    }
}
