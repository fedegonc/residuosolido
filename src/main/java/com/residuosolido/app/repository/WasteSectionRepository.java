package com.residuosolido.app.repository;

import com.residuosolido.app.model.WasteSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteSectionRepository extends JpaRepository<WasteSection, Long> {
    
    List<WasteSection> findByActiveOrderByDisplayOrderAsc(Boolean active);
    
    @Query("SELECT ws FROM WasteSection ws LEFT JOIN FETCH ws.categories ORDER BY ws.displayOrder ASC")
    List<WasteSection> findAllWithCategoriesOrderByDisplayOrderAsc();
    
    @Query("SELECT ws FROM WasteSection ws LEFT JOIN FETCH ws.categories WHERE ws.active = true ORDER BY ws.displayOrder ASC")
    List<WasteSection> findByActiveWithCategoriesOrderByDisplayOrderAsc(Boolean active);
}
