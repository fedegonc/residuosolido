package com.residuosolido.app.repository;

import com.residuosolido.app.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByActiveTrue();
    List<Organization> findByCityAndActiveTrue(String city);
    List<String> findDistinctCityByActiveTrueOrderByCity();
}
