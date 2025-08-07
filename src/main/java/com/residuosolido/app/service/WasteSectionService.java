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
    
    public WasteSection getWasteSectionById(Long id) {
        return wasteSectionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sección de residuos no encontrada con ID: " + id));
    }
    
    @Transactional
    public WasteSection updateWasteSection(Long id, WasteSection wasteSection, Long[] categoryIds) {
        WasteSection existingSection = getWasteSectionById(id);
        
        // Actualizar propiedades
        existingSection.setTitle(wasteSection.getTitle());
        existingSection.setDescription(wasteSection.getDescription());
        existingSection.setIcon(wasteSection.getIcon());
        existingSection.setActionText(wasteSection.getActionText());
        existingSection.setActionUrl(wasteSection.getActionUrl())
        existingSection.setDisplayOrder(wasteSection.getDisplayOrder());
        existingSection.setActive(wasteSection.getActive());
        
        // Limpiar categorías existentes
        existingSection.getCategories().clear();
        
        // Asociar nuevas categorías si se proporcionaron
        if (categoryIds != null && categoryIds.length > 0) {
            for (Long categoryId : categoryIds) {
                categoryService.getCategoryById(categoryId).ifPresent(category -> {
                    existingSection.getCategories().add(category);
                });
            }
        }
        
        return wasteSectionRepository.save(existingSection);
    }
    
    @Transactional
    public void deleteWasteSection(Long id) {
        WasteSection section = getWasteSectionById(id);
        
        // Limpiar relaciones con categorías para evitar problemas de FK
        section.getCategories().clear();
        wasteSectionRepository.save(section);
        
        // Eliminar la sección
        wasteSectionRepository.delete(section);
    }
}
