package com.residuosolido.app.config;

import com.residuosolido.app.repository.InformalCollectorRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class SeedSafetyTest {
    @Test
    void existingUsersAreNeverDeletedOrOverwritten() {
        UserRepository users = mock(UserRepository.class);
        RequestRepository requests = mock(RequestRepository.class);
        InformalCollectorRepository collectors = mock(InformalCollectorRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(users.count()).thenReturn(1L);

        SeedDataFactory.seedAll(users, requests, collectors, encoder);

        verify(users).count();
        verifyNoMoreInteractions(users);
        verifyNoInteractions(requests, collectors, encoder);
    }

    @Test
    void existingRequestsPreventSeedingEvenWithoutUsers() {
        UserRepository users = mock(UserRepository.class);
        RequestRepository requests = mock(RequestRepository.class);
        InformalCollectorRepository collectors = mock(InformalCollectorRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(requests.count()).thenReturn(1L);

        SeedDataFactory.seedAll(users, requests, collectors, encoder);

        verify(users).count();
        verify(requests).count();
        verifyNoMoreInteractions(users, requests);
        verifyNoInteractions(collectors, encoder);
    }

    @Test
    void existingCollectorsPreventSeeding() {
        UserRepository users = mock(UserRepository.class);
        RequestRepository requests = mock(RequestRepository.class);
        InformalCollectorRepository collectors = mock(InformalCollectorRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(collectors.count()).thenReturn(1L);

        SeedDataFactory.seedAll(users, requests, collectors, encoder);

        verify(users).count();
        verify(requests).count();
        verify(collectors).count();
        verifyNoMoreInteractions(users, requests, collectors);
        verifyNoInteractions(encoder);
    }
}
