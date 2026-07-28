package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.InformalCollector;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.InformalCollectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InformalCollectorServiceTest {

    private InformalCollectorRepository repository;
    private InformalCollectorService service;

    private User organization;

    @BeforeEach
    void setUp() {
        repository = mock(InformalCollectorRepository.class);
        service = new InformalCollectorService(repository);

        organization = new User();
        organization.setId("org-1");
    }

    @Test
    void create_validData_savesCollector() {
        when(repository.save(any(InformalCollector.class))).thenAnswer(inv -> inv.getArgument(0));

        InformalCollector result = service.create(organization, "Juan", "099123456", City.RIVERA,
                List.of(MaterialCategory.PAPEL), "nota");

        assertEquals("org-1", result.getOrganizationId());
        assertEquals("Juan", result.getName());
        assertEquals("099123456", result.getPhone());
        assertEquals(City.RIVERA, result.getCity());
        assertTrue(result.isActive());
        assertNotNull(result.getCreatedAt());
        verify(repository).save(any(InformalCollector.class));
    }

    @Test
    void create_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(organization, null, "099123456", City.RIVERA, List.of(), null));
    }

    @Test
    void create_shortPhone_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(organization, "Juan", "123", City.RIVERA, List.of(), null));
    }

    @Test
    void findOwnedById_correctOwner_returnsCollector() {
        InformalCollector collector = InformalCollector.create("org-1", "Juan", "099123456", City.RIVERA, List.of(), null);
        when(repository.findById("id-1")).thenReturn(Optional.of(collector));

        InformalCollector result = service.findOwnedById("id-1", organization);

        assertSame(collector, result);
    }

    @Test
    void findOwnedById_notFound_throwsIllegalArgument() {
        when(repository.findById("id-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.findOwnedById("id-1", organization));
    }

    @Test
    void findOwnedById_wrongOwner_throwsSecurity() {
        InformalCollector collector = InformalCollector.create("org-otra", "Juan", "099123456", City.RIVERA, List.of(), null);
        when(repository.findById("id-1")).thenReturn(Optional.of(collector));

        assertThrows(SecurityException.class,
                () -> service.findOwnedById("id-1", organization));
    }

    @Test
    void update_correctOwner_updatesAndSaves() {
        InformalCollector collector = InformalCollector.create("org-1", "Juan", "099123456", City.RIVERA, List.of(), null);
        when(repository.findById("id-1")).thenReturn(Optional.of(collector));
        when(repository.save(any(InformalCollector.class))).thenAnswer(inv -> inv.getArgument(0));

        InformalCollector result = service.update("id-1", organization, "Pedro", "099654321",
                City.LIVRAMENTO, List.of(MaterialCategory.PLASTIC), "nueva nota", false);

        assertEquals("Pedro", result.getName());
        assertEquals("099654321", result.getPhone());
        assertEquals(City.LIVRAMENTO, result.getCity());
        assertFalse(result.isActive());
        verify(repository).save(collector);
    }

    @Test
    void delete_correctOwner_deletesCollector() {
        InformalCollector collector = InformalCollector.create("org-1", "Juan", "099123456", City.RIVERA, List.of(), null);
        when(repository.findById("id-1")).thenReturn(Optional.of(collector));

        service.delete("id-1", organization);

        verify(repository).delete(collector);
    }

    @Test
    void delete_wrongOwner_throwsSecurity() {
        InformalCollector collector = InformalCollector.create("org-otra", "Juan", "099123456", City.RIVERA, List.of(), null);
        when(repository.findById("id-1")).thenReturn(Optional.of(collector));

        assertThrows(SecurityException.class, () -> service.delete("id-1", organization));
        verify(repository, never()).delete(any(InformalCollector.class));
    }
}
