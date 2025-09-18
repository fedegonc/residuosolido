package com.residuosolido.app.service;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.ui.Model;

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
        try {
            if (categoryRepository.count() == 0) {
                System.out.println("Inicializando categorías por defecto...");
                
                // Categorías principales con campos extendidos
                Category reciclable = new Category(null, "Reciclable");
                reciclable.setDescription("Materiales que pueden ser reciclados");
                reciclable.setDisplayOrder(1);
                reciclable.setActive(true);
                reciclable.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
                categoryRepository.save(reciclable);
                System.out.println("Categoría creada: " + reciclable.getName());
                
                Category noReciclable = new Category(null, "No Reciclable");
                noReciclable.setDescription("Residuos que requieren disposición especial");
                noReciclable.setDisplayOrder(2);
                noReciclable.setActive(true);
                noReciclable.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
                categoryRepository.save(noReciclable);
                System.out.println("Categoría creada: " + noReciclable.getName());
                
                Category informaciones = new Category(null, "Informaciones");
                informaciones.setDescription("Guías y recursos educativos");
                informaciones.setDisplayOrder(3);
                informaciones.setActive(true);
                informaciones.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
                categoryRepository.save(informaciones);
                System.out.println("Categoría creada: " + informaciones.getName());
                
                // Categorías adicionales
                createSimpleCategory("Compostaje", 4);
                createSimpleCategory("Reducción de Residuos", 5);
                createSimpleCategory("Educación Ambiental", 6);
                createSimpleCategory("Normativas", 7);
                
                System.out.println("Inicialización de categorías completada. Total: " + categoryRepository.count());
                
                // Invalidar cache
                cachedCategories = null;
            } else {
                System.out.println("Las categorías ya están inicializadas. Total: " + categoryRepository.count());
            }
        } catch (Exception e) {
            System.err.println("Error al inicializar categorías: " + e.getMessage());
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
            System.out.println("Categoría adicional creada: " + name);
        } catch (Exception e) {
            System.err.println("Error al crear categoría '" + name + "': " + e.getMessage());
        }
    }

    public List<Category> getAllCategories() {
        try {
            if (cachedCategories == null) {
                cachedCategories = categoryRepository.findAll();
                System.out.println("Cargando categorías desde la base de datos. Total: " + cachedCategories.size());
            }
            return cachedCategories;
        } catch (Exception e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Devolver lista vacía en caso de error
        }
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

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
    
    /**
     * Retorna solo las categorías activas ordenadas por orden de visualización
     */
    public List<Category> findAllActive() {
        try {
            List<Category> activeCategories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
            System.out.println("Categorías activas cargadas. Total: " + activeCategories.size());
            return activeCategories;
        } catch (Exception e) {
            System.err.println("Error al obtener categorías activas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Devolver lista vacía en caso de error
        }
    }

    public long count() {
        return categoryRepository.count();
    }

    public Category save(Category category) {
        cachedCategories = null;
        return categoryRepository.save(category);
    }

    public String saveCategory(Category category) {
        if (category.getId() != null) {
            getCategoryById(category.getId()).ifPresent(existing -> {
                existing.setName(category.getName());
                existing.setDescription(category.getDescription());
                existing.setImageUrl(category.getImageUrl());
                existing.setDisplayOrder(category.getDisplayOrder());
                existing.setActive(category.getActive());
                save(existing);
            });
            return "Categoría actualizada correctamente";
        } else {
            if (category.getActive() == null) category.setActive(true);
            save(category);
            return "Categoría creada correctamente";
        }
    }

    public void prepareCategoryListView(Model model) {
        List<Category> categories = findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("totalCategories", categories.size());
        model.addAttribute("viewType", "list");
    }

    public void prepareCategoryFormView(Model model, Long categoryId, boolean isEdit) {
        model.addAttribute("viewType", "form");
        model.addAttribute("isEdit", isEdit);
        
        if (isEdit && categoryId != null) {
            Category category = getCategoryById(categoryId).orElse(null);
            if (category != null) {
                model.addAttribute("category", category);
            } else {
                model.addAttribute("errorMessage", "Categoría no encontrada");
                prepareCategoryListView(model);
            }
        } else {
            model.addAttribute("category", new Category());
        }
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
