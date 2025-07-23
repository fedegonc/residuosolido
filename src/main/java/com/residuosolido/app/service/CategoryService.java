package com.residuosolido.app.service;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initializeCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "Reciclaje"));
            categoryRepository.save(new Category(null, "Compostaje"));
            categoryRepository.save(new Category(null, "Reducción de Residuos"));
            categoryRepository.save(new Category(null, "Educación Ambiental"));
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<java.util.Map<String, Object>> getCategoriesWithSlugs() {
        return categoryRepository.findAll().stream()
            .map(category -> {
                java.util.Map<String, Object> categoryData = new java.util.HashMap<>();
                categoryData.put("id", category.getId());
                categoryData.put("name", category.getName());
                categoryData.put("slug", generateSlug(category.getName()));
                return categoryData;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    public java.util.Optional<Category> findBySlug(String slug) {
        return categoryRepository.findAll().stream()
            .filter(cat -> generateSlug(cat.getName()).equals(slug))
            .findFirst();
    }
    
    public String generateSlug(String text) {
        if (text == null) return "";
        
        // Normalizar caracteres especiales
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        
        // Convertir a minúsculas, reemplazar espacios y caracteres especiales
        return normalized.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
}
