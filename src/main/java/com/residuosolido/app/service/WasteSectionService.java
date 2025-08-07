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
        System.out.println("=== INICIO createWasteSection ===");
        System.out.println("WasteSection recibida: " + wasteSection);
        System.out.println("Title: " + wasteSection.getTitle());
        System.out.println("Description: " + wasteSection.getDescription());
        System.out.println("Icon: " + wasteSection.getIcon());
        System.out.println("ActionText: " + wasteSection.getActionText());
        System.out.println("DisplayOrder: " + wasteSection.getDisplayOrder());
        System.out.println("Active: " + wasteSection.getActive());
        System.out.println("CategoryIds: " + (categoryIds != null ? Arrays.toString(categoryIds) : "null"));
        
        // Setear valores por defecto
        if (wasteSection.getActive() == null) {
            wasteSection.setActive(true);
            System.out.println("Active seteado a true por defecto");
        }
        if (wasteSection.getDisplayOrder() == null) {
            Integer nextOrder = getNextDisplayOrder();
            wasteSection.setDisplayOrder(nextOrder);
            System.out.println("DisplayOrder seteado a: " + nextOrder);
        }
        
        System.out.println("=== ANTES DE GUARDAR ===");
        System.out.println("WasteSection a guardar: " + wasteSection);
        
        // Guardar la sección primero
        WasteSection savedSection = wasteSectionRepository.save(wasteSection);
        System.out.println("=== DESPUÉS DE GUARDAR ===");
        System.out.println("WasteSection guardada: " + savedSection);
        System.out.println("ID generado: " + savedSection.getId());
        
        // Asociar categorías si se proporcionaron
        if (categoryIds != null && categoryIds.length > 0) {
            System.out.println("=== ASOCIANDO CATEGORÍAS ===");
            final WasteSection finalSection = savedSection; // Variable final para lambda
            for (Long categoryId : categoryIds) {
                System.out.println("Buscando categoría con ID: " + categoryId);
                categoryService.getCategoryById(categoryId).ifPresent(category -> {
                    System.out.println("Categoría encontrada: " + category.getName());
                    finalSection.getCategories().add(category);
                });
            }
            System.out.println("Categorías asociadas: " + savedSection.getCategories().size());
            // Guardar nuevamente con las categorías
            savedSection = wasteSectionRepository.save(savedSection);
            System.out.println("=== GUARDADO FINAL CON CATEGORÍAS ===");
        }
        
        System.out.println("=== FIN createWasteSection ===");
        return savedSection;
    }
    
    private Integer getNextDisplayOrder() {
        List<WasteSection> sections = wasteSectionRepository.findAll();
        return sections.stream()
                .mapToInt(s -> s.getDisplayOrder() != null ? s.getDisplayOrder() : 0)
                .max()
                .orElse(0) + 1;
    }
    
    public List<WasteSection> getActiveSectionsWithCategories() {
        return wasteSectionRepository.findByActiveWithCategoriesOrderByDisplayOrderAsc(true);
    }
    
    public List<WasteSection> getAllOrderedByDisplayOrder() {
        return wasteSectionRepository.findAllWithCategoriesOrderByDisplayOrderAsc();
    }
}
