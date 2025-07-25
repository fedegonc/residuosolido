package com.residuosolido.app.service;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

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
            // Categorías jerárquicas principales
            categoryRepository.save(new Category(null, "Reciclable"));
            categoryRepository.save(new Category(null, "No Reciclable"));
            categoryRepository.save(new Category(null, "Informaciones"));
            
            // Categorías adicionales
            categoryRepository.save(new Category(null, "Compostaje"));
            categoryRepository.save(new Category(null, "Reducción de Residuos"));
            categoryRepository.save(new Category(null, "Educación Ambiental"));
            categoryRepository.save(new Category(null, "Normativas"));
            categoryRepository.save(new Category(null, "Tecnología Verde"));
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
    
    public Optional<Category> findBySlug(String slug) {
        return getAllCategories().stream().filter(cat -> generateSlug(cat.getName()).equals(slug)).findFirst();
    }

    public String getCategoryNameById(Long categoryId) {
        return getAllCategories().stream().filter(cat -> cat.getId().equals(categoryId)).findFirst().map(Category::getName).orElse("Sin categoría");
    }
    
    public Category createCategory(String name) {
        Category category = new Category(null, name);
        return categoryRepository.save(category);
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Category updateCategory(Long id, String name) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setName(name);
            return categoryRepository.save(category);
        }
        return null;
    }
    
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
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
