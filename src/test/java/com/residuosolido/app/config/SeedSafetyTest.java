package com.residuosolido.app.config;

import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@Tag("unit")
class SeedSafetyTest {
    @Test
    void existingUsersAreNeverDeletedOrOverwritten() {
        UserRepository users = mock(UserRepository.class);
        RequestRepository requests = mock(RequestRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(users.count()).thenReturn(1L);

        DataLoader.seedAll(users, requests, encoder);

        verify(users).count();
        verifyNoMoreInteractions(users);
        verifyNoInteractions(requests, encoder);
    }

    @Test
    void existingRequestsPreventSeedingEvenWithoutUsers() {
        UserRepository users = mock(UserRepository.class);
        RequestRepository requests = mock(RequestRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(requests.count()).thenReturn(1L);

        DataLoader.seedAll(users, requests, encoder);

        verify(users).count();
        verify(requests).count();
        verifyNoMoreInteractions(users, requests);
        verifyNoInteractions(encoder);
    }
}
