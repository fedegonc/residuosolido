package com.residuosolido.app.controller;

import com.residuosolido.app.config.DataSeeder;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class SeedControllerTest {

    private SeedController createWithProfiles(String... profiles) {
        UserRepository userRepo = mock(UserRepository.class);
        OrganizationRepository orgRepo = mock(OrganizationRepository.class);
        RequestRepository requestRepo = mock(RequestRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(profiles);
        return new SeedController(userRepo, orgRepo, requestRepo,
                new DataSeeder(userRepo, orgRepo, requestRepo, encoder), env);
    }

    @Test
    void constructor_acceptsDevProfile() {
        createWithProfiles("dev");
    }

    @Test
    void constructor_acceptsTestProfile() {
        createWithProfiles("test");
    }

    @Test
    void constructor_rejectsProductionProfile() {
        assertThrows(IllegalStateException.class,
                () -> createWithProfiles("prod"));
    }

    @Test
    void seed_withForce_deletesAllRepositories() {
        UserRepository userRepo = mock(UserRepository.class);
        OrganizationRepository orgRepo = mock(OrganizationRepository.class);
        RequestRepository requestRepo = mock(RequestRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded");
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});

        when(userRepo.count()).thenReturn(0L);
        when(requestRepo.count()).thenReturn(0L);
        when(userRepo.insert(any(com.residuosolido.app.model.User.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any(com.residuosolido.app.model.User.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(orgRepo.save(any(com.residuosolido.app.model.Organization.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SeedController controller = new SeedController(userRepo, orgRepo, requestRepo,
                new DataSeeder(userRepo, orgRepo, requestRepo, encoder), env);
        controller.seed(true);

        verify(requestRepo).deleteAll();
        verify(orgRepo).deleteAll();
        verify(userRepo).deleteAll();
    }
}
