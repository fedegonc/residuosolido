package com.residuosolido.app.service;

import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Tag("unit")
class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository);
        userRegistrationService = new UserRegistrationService(userRepository, passwordEncoder);
    }

    // ===== PIN de 4 dígitos en registro (fricción mínima para pruebas, ver DEFENSA.md §24) =====

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

    // ===== registerUser =====

    @Test
    void registerUser_encodesPasswordAndSetsDefaults() {
        User user = new User();
        user.setUsername("newuser");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userRegistrationService.registerUser(user, false);
        assertEquals("encoded", result.getPassword());
        assertEquals(Role.USER, result.getRole());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void registerUser_asOrganization_setsOrganizationRole() {
        User user = new User();
        user.setUsername("org1");
        user.setPhone("+59899123456");
        user.setPassword("1234");

        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.insert(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userRegistrationService.registerUser(user, true);
        assertEquals(Role.ORGANIZATION, result.getRole());
    }

    // ===== updateProfile en orgs: auto-completa el perfil cuando hay phone + city =====

    @Test
    void updateProfile_orgWithoutPhone_doesNotCompleteProfile() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");
        org.setRole(Role.ORGANIZATION);

        when(userRepository.findById("1")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile(org, null, null, null, City.RIVERA, null);

        assertNotEquals(Boolean.TRUE, org.getProfileCompleted());
    }

    @Test
    void updateProfile_orgWithoutCity_doesNotCompleteProfile() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");
        org.setRole(Role.ORGANIZATION);

        when(userRepository.findById("1")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile(org, null, null, "+59899123456", null, null);

        assertNotEquals(Boolean.TRUE, org.getProfileCompleted());
    }

    @Test
    void updateProfile_invalidPhoneFormat_throwsPhoneInvalid() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");
        org.setRole(Role.ORGANIZATION);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(org, null, null, "099123456", City.RIVERA, null));
        assertEquals("error.phone.invalid", ex.getMessage());
    }

    @Test
    void updateProfile_orgWithPhoneAndCity_completesProfile() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");
        org.setRole(Role.ORGANIZATION);

        when(userRepository.findById("1")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile(org, null, null, "+598 99 123 456", City.RIVERA, null);

        assertTrue(org.getProfileCompleted());
        assertEquals(City.RIVERA, org.getCity());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_withMaterials_persistsAcceptedMaterials() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        when(userRepository.findById("1")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile(org, null, null, null, null,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO));

        assertEquals(List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO), org.getAcceptedMaterials());
    }

    @Test
    void updateProfile_emptyMaterialsList_clearsAcceptedMaterials() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");
        org.setAcceptedMaterials(List.of(MaterialCategory.PLASTICO));

        when(userRepository.findById("1")).thenReturn(Optional.of(org));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateProfile(org, null, null, null, null, List.of());

        assertTrue(org.getAcceptedMaterials().isEmpty());
    }
}
