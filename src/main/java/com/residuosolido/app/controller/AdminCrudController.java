package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador unificado para CRUD de todas las entidades del sistema
 */
@Controller
@RequestMapping("/admin/crud")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCrudController {

    @Autowired private UserService userService;
    @Autowired private PostService postService;
    @Autowired private CategoryService categoryService;
    @Autowired private MaterialService materialService;
    @Autowired private RequestService requestService;
    @Autowired private FeedbackService feedbackService;

    /**
     * Página principal del CRUD para una entidad específica
     */
    @GetMapping("/{entity}")
    public String crudPage(@PathVariable String entity, Model model) {
        model.addAttribute("entityName", entity);
        return "admin/entity-crud";
    }

    // ========== USERS API ==========
    
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = search.isEmpty() ? 
            userService.findAll(pageable) : 
            userService.searchUsers(search, pageable);
        
        return ResponseEntity.ok(users);
    }

    @PostMapping("/api/users")
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== POSTS API ==========
    
    @GetMapping("/api/posts")
    @ResponseBody
    public ResponseEntity<List<Post>> getPosts(
            @RequestParam(defaultValue = "") String search) {
        
        List<Post> posts = postService.findAllWithCategories();
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/api/posts")
    @ResponseBody
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post savedPost = postService.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @PutMapping("/api/posts/{id}")
    @ResponseBody
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post) {
        post.setId(id);
        Post updatedPost = postService.save(post);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/api/posts/{id}")
    @ResponseBody
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== CATEGORIES API ==========
    
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<Category>> getCategories() {
        List<Category> categories = categoryService.findAllOrderedByDisplayOrder();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryService.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    @PutMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        Category updatedCategory = categoryService.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== MATERIALS API ==========
    
    @GetMapping("/api/materials")
    @ResponseBody
    public ResponseEntity<List<Material>> getMaterials() {
        List<Material> materials = materialService.findAll();
        return ResponseEntity.ok(materials);
    }

    @PostMapping("/api/materials")
    @ResponseBody
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        Material savedMaterial = materialService.save(material);
        return ResponseEntity.ok(savedMaterial);
    }

    @PutMapping("/api/materials/{id}")
    @ResponseBody
    public ResponseEntity<Material> updateMaterial(@PathVariable Long id, @RequestBody Material material) {
        material.setId(id);
        Material updatedMaterial = materialService.save(material);
        return ResponseEntity.ok(updatedMaterial);
    }

    @DeleteMapping("/api/materials/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== REQUESTS API ==========
    
    @GetMapping("/api/requests")
    @ResponseBody
    public ResponseEntity<List<Request>> getRequests() {
        List<Request> requests = requestService.findAll();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/api/requests")
    @ResponseBody
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        Request savedRequest = requestService.save(request);
        return ResponseEntity.ok(savedRequest);
    }

    @PutMapping("/api/requests/{id}")
    @ResponseBody
    public ResponseEntity<Request> updateRequest(@PathVariable Long id, @RequestBody Request request) {
        request.setId(id);
        Request updatedRequest = requestService.save(request);
        return ResponseEntity.ok(updatedRequest);
    }

    @DeleteMapping("/api/requests/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== FEEDBACK API ==========
    
    @GetMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<List<Feedback>> getFeedback() {
        List<Feedback> feedback = feedbackService.findAllOrderedByCreatedAtDesc();
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        Feedback savedFeedback = feedbackService.save(feedback);
        return ResponseEntity.ok(savedFeedback);
    }

    @PutMapping("/api/feedback/{id}")
    @ResponseBody
    public ResponseEntity<Feedback> updateFeedback(@PathVariable Long id, @RequestBody Feedback feedback) {
        feedback.setId(id);
        Feedback updatedFeedback = feedbackService.save(feedback);
        return ResponseEntity.ok(updatedFeedback);
    }

    @DeleteMapping("/api/feedback/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ========== UTILITY METHODS ==========
    
    /**
     * Endpoint para obtener metadatos de una entidad (campos, tipos, etc.)
     */
    @GetMapping("/api/{entity}/metadata")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEntityMetadata(@PathVariable String entity) {
        Map<String, Object> metadata = new HashMap<>();
        
        switch (entity.toLowerCase()) {
            case "users":
                metadata.put("fields", getUserFields());
                break;
            case "posts":
                metadata.put("fields", getPostFields());
                break;
            case "categories":
                metadata.put("fields", getCategoryFields());
                break;
            case "materials":
                metadata.put("fields", getMaterialFields());
                break;
            case "requests":
                metadata.put("fields", getRequestFields());
                break;
            case "feedback":
                metadata.put("fields", getFeedbackFields());
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(metadata);
    }

    private Map<String, Object>[] getUserFields() {
        return new Map[] {
            Map.of("name", "username", "label", "Usuario", "type", "text", "required", true),
            Map.of("name", "email", "label", "Email", "type", "email", "required", true),
            Map.of("name", "firstName", "label", "Nombre", "type", "text"),
            Map.of("name", "lastName", "label", "Apellido", "type", "text"),
            Map.of("name", "role", "label", "Rol", "type", "select", "options", new Map[] {
                Map.of("value", "USER", "label", "Usuario"),
                Map.of("value", "ORGANIZATION", "label", "Organización"),
                Map.of("value", "ADMIN", "label", "Administrador")
            }),
            Map.of("name", "active", "label", "Activo", "type", "checkbox")
        };
    }

    private Map<String, Object>[] getPostFields() {
        return new Map[] {
            Map.of("name", "title", "label", "Título", "type", "text", "required", true),
            Map.of("name", "content", "label", "Contenido", "type", "textarea", "required", true),
            Map.of("name", "imageUrl", "label", "URL Imagen", "type", "url"),
            Map.of("name", "sourceUrl", "label", "URL Fuente", "type", "url"),
            Map.of("name", "sourceName", "label", "Nombre Fuente", "type", "text")
        };
    }

    private Map<String, Object>[] getCategoryFields() {
        return new Map[] {
            Map.of("name", "name", "label", "Nombre", "type", "text", "required", true),
            Map.of("name", "description", "label", "Descripción", "type", "textarea"),
            Map.of("name", "imageUrl", "label", "URL Imagen", "type", "url"),
            Map.of("name", "displayOrder", "label", "Orden", "type", "number"),
            Map.of("name", "active", "label", "Activo", "type", "checkbox")
        };
    }

    private Map<String, Object>[] getMaterialFields() {
        return new Map[] {
            Map.of("name", "name", "label", "Nombre", "type", "text", "required", true),
            Map.of("name", "description", "label", "Descripción", "type", "textarea"),
            Map.of("name", "category", "label", "Categoría", "type", "text"),
            Map.of("name", "active", "label", "Activo", "type", "checkbox")
        };
    }

    private Map<String, Object>[] getRequestFields() {
        return new Map[] {
            Map.of("name", "description", "label", "Descripción", "type", "textarea", "required", true),
            Map.of("name", "collectionAddress", "label", "Dirección", "type", "text"),
            Map.of("name", "scheduledDate", "label", "Fecha Programada", "type", "date"),
            Map.of("name", "status", "label", "Estado", "type", "select", "options", new Map[] {
                Map.of("value", "PENDING", "label", "Pendiente"),
                Map.of("value", "ACCEPTED", "label", "Aceptado"),
                Map.of("value", "REJECTED", "label", "Rechazado"),
                Map.of("value", "COMPLETED", "label", "Completado")
            }),
            Map.of("name", "notes", "label", "Notas", "type", "textarea")
        };
    }

    private Map<String, Object>[] getFeedbackFields() {
        return new Map[] {
            Map.of("name", "name", "label", "Nombre", "type", "text", "required", true),
            Map.of("name", "email", "label", "Email", "type", "email"),
            Map.of("name", "comment", "label", "Comentario", "type", "textarea", "required", true)
        };
    }
}
