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
        try {
            if (categoryRepository.count() == 0) {
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
                
                createSimpleCategory("Compostaje", 4);
                createSimpleCategory("Reducción de Residuos", 5);
                createSimpleCategory("Educación Ambiental", 6);
                createSimpleCategory("Normativas", 7);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void createSimpleCategory(String name, int order) {
        try {
            Category category = new Category(null, name);
            category.setDisplayOrder(order);
            category.setActive(true);
            category.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            categoryRepository.save(category);
        } catch (Exception e) {
            // Error creating category
        }
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepository.findById(id);
    }

    public List<Category> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query.trim(), query.trim());
    }
    
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    /**
     * Retorna solo las categorías activas ordenadas por orden de visualización
     */
    public List<Category> findAllActive() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

}
