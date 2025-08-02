package com.residuosolido.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataCleanupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void cleanupWasteSectionCategories() {
        // 1. Eliminar registros con waste_section_id inexistente
        String deleteOrphanWasteSections = """
            DELETE FROM waste_section_categories 
            WHERE waste_section_id NOT IN (SELECT id FROM waste_sections)
        """;
        
        // 2. Eliminar registros con category_id inexistente  
        String deleteOrphanCategories = """
            DELETE FROM waste_section_categories 
            WHERE category_id NOT IN (SELECT id FROM categories)
        """;
        
        // 3. Eliminar duplicados
        String deleteDuplicates = """
            DELETE FROM waste_section_categories 
            WHERE (waste_section_id, category_id) IN (
                SELECT waste_section_id, category_id 
                FROM (
                    SELECT waste_section_id, category_id, 
                           ROW_NUMBER() OVER (PARTITION BY waste_section_id, category_id ORDER BY waste_section_id) as rn
                    FROM waste_section_categories
                ) t WHERE rn > 1
            )
        """;
        
        int orphanWaste = jdbcTemplate.update(deleteOrphanWasteSections);
        int orphanCat = jdbcTemplate.update(deleteOrphanCategories);
        int duplicates = jdbcTemplate.update(deleteDuplicates);
        
        System.out.println("✅ CLEANUP COMPLETADO:");
        System.out.println("   - Waste sections huérfanos: " + orphanWaste);
        System.out.println("   - Categorías huérfanas: " + orphanCat);
        System.out.println("   - Duplicados: " + duplicates);
    }
    


}
