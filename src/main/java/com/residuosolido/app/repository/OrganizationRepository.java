package com.residuosolido.app.repository;

import com.residuosolido.app.model.Organization;
import com.residuosolido.app.enums.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends MongoRepository<Organization, String> {

    List<Organization> findByCity(City city);
}
