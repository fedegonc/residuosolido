package com.residuosolido.app.repository;

import com.residuosolido.app.model.WasteSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteSectionRepository extends JpaRepository<WasteSection, Long> {
    
    List<WasteSection> findByActiveOrderByDisplayOrderAsc(Boolean active);
    List<WasteSection> findAllByOrderByDisplayOrderAsc();
}
