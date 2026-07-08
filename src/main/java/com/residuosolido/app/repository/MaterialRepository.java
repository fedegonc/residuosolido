package com.residuosolido.app.repository;

import com.residuosolido.app.model.Material;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends MongoRepository<Material, String> {
    List<Material> findByActiveTrue();
    Material findByName(String name);

    List<Material> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String description, String category);
}
