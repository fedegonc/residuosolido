package com.residuosolido.app.repository;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.WasteSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByWasteSection(WasteSection wasteSection);
    
    List<Category> findByWasteSectionId(Long wasteSectionId);
}
