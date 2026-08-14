package com.residuosolido.app.repository;

import com.residuosolido.app.model.InformalCollector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InformalCollectorRepository extends MongoRepository<InformalCollector, String> {

    List<InformalCollector> findByOrganizationIdOrderByNameAsc(String organizationId);
}
