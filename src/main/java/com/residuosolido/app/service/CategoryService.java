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
    private List<Category> cachedCategories = null;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initializeCategories() {
        if (categoryRepository.count() == 0) {
            // Categorías principales con campos extendidos
            Category reciclable = new Category(null, "Reciclable");
            reciclable.setDescription("Materiales que pueden ser reciclados");
            reciclable.setDisplayOrder(1);
            reciclable.setActive(true);
            reciclable.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            categoryRepository.save(reciclable);
            
            Category noReciclable = new Category(null, "No Reciclable");
            noReciclable.setDescription("Residuos que requieren disposición especial");
            noReciclable.setDisplayOrder(2);
            noReciclable.setActive(true);
            noReciclable.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            categoryRepository.save(noReciclable);
            
            Category informaciones = new Category(null, "Informaciones");
            informaciones.setDescription("Guías y recursos educativos");
            informaciones.setDisplayOrder(3);
            informaciones.setActive(true);
            informaciones.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            categoryRepository.save(informaciones);
            
            // Categorías adicionales
            createSimpleCategory("Compostaje", 4);
            createSimpleCategory("Reducción de Residuos", 5);
            createSimpleCategory("Educación Ambiental", 6);
            createSimpleCategory("Normativas", 7);
        }
    }
    
    private void createSimpleCategory(String name, int order) {
        Category category = new Category(null, name);
        category.setDisplayOrder(order);
        category.setActive(true);
        category.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        categoryRepository.save(category);
    }

    public List<Category> getAllCategories() {
        if (cachedCategories == null) {
            cachedCategories = categoryRepository.findAll();
        }
        return cachedCategories;
    }
    
    // Métodos que reemplazan WasteSectionService
    public List<Category> getActiveCategoriesOrderedByDisplayOrder() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }
    
    public List<Category> getAllCategoriesOrderedByDisplayOrder() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
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
        cachedCategories = null; // Invalidar cache
        return categoryRepository.save(category);
    }
    
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    public Category updateCategory(Long id, String name) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            categoryRepository.findById(id).ifPresent(category -> {
                category.setName(name);
                categoryRepository.save(category);
                cachedCategories = null; // Invalidar cache
            });
            return categoryOpt.get();
        }
        return null;
    }
    
    public boolean deleteCategory(Long id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            cachedCategories = null; // Invalidar cache
            return true;
        }
        return false;
    }
    
    public List<Category> findAllOrderedByDisplayOrder() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Category save(Category category) {
        cachedCategories = null;
        return categoryRepository.save(category);
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
        cachedCategories = null;
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
