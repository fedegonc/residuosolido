package com.residuosolido.app.repository;

import com.residuosolido.app.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByActiveTrue();

    @Query("SELECT m.category, COUNT(m) FROM Material m WHERE m.category IS NOT NULL GROUP BY m.category")
    List<Object[]> countMaterialsByCategory();
}
