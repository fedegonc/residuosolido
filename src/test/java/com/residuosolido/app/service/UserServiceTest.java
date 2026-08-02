package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private UserRegistrationService userRegistrationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
        userRegistrationService = new UserRegistrationService(userRepository, passwordEncoder);
    }

    // ===== Password mínimo 3 chars en registro =====

    @Test
    void validateUserRegistration_shortPassword_returnsError() {
        User user = new User();
        user.setUsername("nuevo");
        user.setEmail("nuevo@test.com");
        user.setPassword("12");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        String error = userRegistrationService.validateUserRegistration(user);
        assertNotNull(error);
        assertTrue(error.contains("error.register.password_min_length"));
    }

    @Test
    void validateUserRegistration_nullPassword_returnsError() {
        User user = new User();
        user.setUsername("nuevo");
        user.setEmail("nuevo@test.com");
        user.setPassword(null);

        String error = userRegistrationService.validateUserRegistration(user);
        assertNotNull(error);
        assertTrue(error.contains("error.register.password_min_length"));
    }

    @Test
    void validateUserRegistration_validPassword_returnsNull() {
        User user = new User();
        user.setUsername("nuevo");
        user.setEmail("nuevo@test.com");
        user.setPassword("password123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertNull(userRegistrationService.validateUserRegistration(user));
    }

    @Test
    void validateUserRegistration_exactly3Chars_returnsNull() {
        User user = new User();
        user.setUsername("nuevo");
        user.setEmail("nuevo@test.com");
        user.setPassword("123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertNull(userRegistrationService.validateUserRegistration(user));
    }

    // ===== Password mínimo 3 chars en update =====

    @Test
    void updateUser_shortPassword_throwsException() {
        User existing = new User();
        existing.setId("123");
        existing.setUsername("user1");
        existing.setEmail("user1@test.com");
        existing.setCity(City.RIVERA);

        User update = new User();
        update.setId("123");
        update.setEmail("user1@test.com");
        update.setCity(City.RIVERA);

        when(userRepository.findById("123")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(update, "12"));
        assertTrue(ex.getMessage().contains("error.register.password_min_length"));
    }

    @Test
    void updateUser_validPassword_encodesAndSaves() {
        User existing = new User();
        existing.setId("123");
        existing.setUsername("user1");
        existing.setEmail("user1@test.com");
        existing.setCity(City.RIVERA);

        User update = new User();
        update.setId("123");
        update.setEmail("updated@test.com");
        update.setCity(City.RIVERA);

        when(userRepository.findById("123")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(existing);

        User result = userService.updateUser(update, "password123");
        assertEquals("encoded", result.getPassword());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_nullPassword_doesNotChangePassword() {
        User existing = new User();
        existing.setId("123");
        existing.setUsername("user1");
        existing.setEmail("user1@test.com");
        existing.setCity(City.RIVERA);
        existing.setPassword("oldencoded");

        User update = new User();
        update.setId("123");
        update.setEmail("updated@test.com");
        update.setCity(City.RIVERA);

        when(userRepository.findById("123")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.updateUser(update, null);
        assertEquals("oldencoded", existing.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_emptyPassword_doesNotChangePassword() {
        User existing = new User();
        existing.setId("123");
        existing.setUsername("user1");
        existing.setEmail("user1@test.com");
        existing.setCity(City.RIVERA);
        existing.setPassword("oldencoded");

        User update = new User();
        update.setId("123");
        update.setEmail("updated@test.com");
        update.setCity(City.RIVERA);

        when(userRepository.findById("123")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        userService.updateUser(update, "   ");
        assertEquals("oldencoded", existing.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ===== registerUser =====

    @Test
    void registerUser_encodesPasswordAndSetsDefaults() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

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
        user.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userRegistrationService.registerUser(user, true);
        assertEquals(Role.ORGANIZATION, result.getRole());
    }

    // ===== completeOrgProfile: validaciones en capa de dominio =====

    @Test
    void completeOrgProfile_nullPhone_throwsPhoneRequired() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.completeOrgProfile(org, null, City.RIVERA));
        assertEquals("error.profile.phone_required", ex.getMessage());
    }

    @Test
    void completeOrgProfile_blankPhone_throwsPhoneRequired() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.completeOrgProfile(org, "   ", City.RIVERA));
        assertEquals("error.phone.required", ex.getMessage());
    }

    @Test
    void completeOrgProfile_invalidPhoneFormat_throwsPhoneInvalid() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.completeOrgProfile(org, "099123456", City.RIVERA));
        assertEquals("error.phone.invalid", ex.getMessage());
    }

    @Test
    void completeOrgProfile_nullCity_throwsCityRequired() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.completeOrgProfile(org, "+598 99 123 456", null));
        assertEquals("error.profile.city_required", ex.getMessage());
    }

    @Test
    void completeOrgProfile_validPhoneAndCity_marksProfileCompleted() {
        User org = new User();
        org.setId("1");
        org.setUsername("coop");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.completeOrgProfile(org, "+598 99 123 456", City.RIVERA);

        assertTrue(org.getProfileCompleted());
        assertEquals(City.RIVERA, org.getCity());
        verify(userRepository).save(org);
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
