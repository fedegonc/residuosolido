package com.residuosolido.app.repository;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.InformalCollector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformalCollectorRepository extends MongoRepository<InformalCollector, String> {

    List<InformalCollector> findByOrganizationIdAndActiveOrderByNameAsc(String organizationId, boolean active);

    List<InformalCollector> findByOrganizationIdOrderByNameAsc(String organizationId);

    long countByCityAndActive(City city, boolean active);

    long countByActive(boolean active);
}
