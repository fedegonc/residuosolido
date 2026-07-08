package com.residuosolido.app.repository;

import com.residuosolido.app.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
    List<Category> findAllByOrderByDisplayOrderAsc();
    Optional<Category> findByName(String name);

    List<Category> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
