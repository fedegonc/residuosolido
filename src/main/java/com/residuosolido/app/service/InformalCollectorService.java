package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.InformalCollector;
import com.residuosolido.app.model.Name;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.InformalCollectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InformalCollectorService {

    private final InformalCollectorRepository repository;

    @Autowired
    public InformalCollectorService(InformalCollectorRepository repository) {
        this.repository = repository;
    }

    public List<InformalCollector> findByOrganization(User organization) {
        return repository.findByOrganizationIdOrderByNameAsc(organization.getId());
    }

    public InformalCollector findOwnedById(String id, User organization) {
        InformalCollector collector = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("error.catador.not_found"));
        if (!collector.belongsTo(organization)) {
            throw new SecurityException("error.catador.no_permission");
        }
        return collector;
    }

    public InformalCollector create(User organization, String name, String phone, City city,
                                    List<MaterialCategory> materials, String notes) {
        validateNameAndPhone(name, phone);
        return repository.save(InformalCollector.create(
                organization.getId(), name, phone, city, materials, notes));
    }

    public InformalCollector update(String id, User organization, String name, String phone, City city,
                                    List<MaterialCategory> materials, String notes, boolean active) {
        validateNameAndPhone(name, phone);
        InformalCollector collector = findOwnedById(id, organization);
        collector.updateDetails(name, phone, city, materials, notes, active);
        return repository.save(collector);
    }

    public void delete(String id, User organization) {
        InformalCollector collector = findOwnedById(id, organization);
        repository.delete(collector);
    }

    public InformalCollector saveOrUpdate(String id, User organization, String name, String phone, City city,
                                          List<MaterialCategory> materials, String notes, boolean active) {
        if (id != null && !id.isBlank()) {
            return update(id, organization, name, phone, city, materials, notes, active);
        }
        return create(organization, name, phone, city, materials, notes);
    }

    private void validateNameAndPhone(String name, String phone) {
        Name.of(name);
        PhoneNumber.of(phone);
    }
}
