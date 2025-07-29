package com.residuosolido.app.repository;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.WasteSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Con la relación ManyToMany, las consultas se manejan desde WasteSection
}
