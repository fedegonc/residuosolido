package com.residuosolido.app.service;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.WasteSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;

@Service
public class WasteSectionService {

    @Autowired
    private WasteSectionRepository wasteSectionRepository;
    
    @Autowired
    private CategoryService categoryService;

    @Transactional
    public WasteSection createWasteSection(WasteSection wasteSection, Long[] categoryIds) {
        // Setear valores por defecto
        if (wasteSection.getActive() == null) {
            wasteSection.setActive(true);
        }
        if (wasteSection.getDisplayOrder() == null) {
            wasteSection.setDisplayOrder(getNextDisplayOrder());
        }
        
        // Guardar la sección primero
        WasteSection savedSection = wasteSectionRepository.save(wasteSection);
        
        // Asociar categorías si se proporcionaron
        if (categoryIds != null && categoryIds.length > 0) {
            final WasteSection finalSection = savedSection; // Variable final para lambda
            for (Long categoryId : categoryIds) {
                categoryService.getCategoryById(categoryId).ifPresent(category -> {
                    finalSection.getCategories().add(category);
                });
            }
            // Guardar nuevamente con las categorías
            savedSection = wasteSectionRepository.save(savedSection);
        }
        
        return savedSection;
    }
    
    private Integer getNextDisplayOrder() {
        List<WasteSection> sections = wasteSectionRepository.findAll();
        return sections.stream()
                .mapToInt(s -> s.getDisplayOrder() != null ? s.getDisplayOrder() : 0)
                .max()
                .orElse(0) + 1;
    }
}
