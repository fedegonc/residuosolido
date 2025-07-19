package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.repository.CategoryRepository;
import com.residuosolido.app.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;  // Añadir esta línea
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminController {
    
    // Datos en memoria
    private static List<Post> posts = new ArrayList<>();
    private static Long nextPostId = 1L;
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FeedbackRepository feedbackRepository;
    
    @Autowired
    public AdminController(UserRepository userRepository, CategoryRepository categoryRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.feedbackRepository = feedbackRepository;
        initCategories();
    }
    
    private void initCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "Reciclaje"));
            categoryRepository.save(new Category(null, "Compostaje"));
            categoryRepository.save(new Category(null, "Educación"));
        }
    }
    
    static {
        // Posts de ejemplo
        posts.add(new Post(nextPostId++, "Campaña de Reciclaje 2025", "Únete a nuestra campaña de reciclaje", "https://via.placeholder.com/400x200", 1L));
        posts.add(new Post(nextPostId++, "Taller de Compostaje", "Aprende a compostar en casa", "https://via.placeholder.com/400x200", 2L));
    }
    
    @GetMapping("/feedback")
    public String listFeedbacks(Model model) {
        model.addAttribute("feedbacks", feedbackRepository.findAllByOrderByCreatedAtDesc());
        return "admin/feedback";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // Obtener estadísticas para el dashboard
        long totalUsers = userRepository.count();
        
        // Agregar datos al modelo
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPosts", posts.size());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("pageTitle", "Panel de Administración");
        
        return "admin/dashboard";
    }
    
    @GetMapping("/posts")
    public String adminPosts(Model model) {
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/posts";
    }
    
    @PostMapping("/posts")
    public String createPost(@RequestParam String title, @RequestParam String content, 
                           @RequestParam String imageUrl, @RequestParam Long categoryId) {
        posts.add(new Post(nextPostId++, title, content, imageUrl, categoryId));
        return "redirect:/admin/posts";
    }
    
    @GetMapping("/posts/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        Optional<Post> postOpt = posts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (postOpt.isPresent()) {
            model.addAttribute("post", postOpt.get());
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/edit-post";
        }
        
        return "redirect:/admin/posts";
    }
    
    @PostMapping("/posts/{id}/update")
    public String updatePost(@PathVariable Long id, 
                           @RequestParam String title, 
                           @RequestParam String content,
                           @RequestParam String imageUrl, 
                           @RequestParam Long categoryId) {
        
        Optional<Post> postOpt = posts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setTitle(title);
            post.setContent(content);
            post.setImageUrl(imageUrl);
            post.setCategoryId(categoryId);
        }
        
        return "redirect:/admin/posts";
    }
    
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        posts.removeIf(p -> p.getId().equals(id));
        return "redirect:/admin/posts";
    }
    
    @PostMapping("/categories")
    public String createCategory(@RequestParam String name) {
        categoryRepository.save(new Category(null, name));
        return "redirect:/admin/posts";
    }
    
    @GetMapping("/categories/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            model.addAttribute("category", categoryOpt.get());
            return "admin/edit-category";
        }
        
        return "redirect:/admin/posts";
    }
    
    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id, @RequestParam String name) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setName(name);
            categoryRepository.save(category);
        }
        
        return "redirect:/admin/posts";
    }
    
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id) {
        // Verificar si hay posts usando esta categoría
        boolean inUse = posts.stream().anyMatch(p -> p.getCategoryId().equals(id));
        
        if (!inUse) {
            categoryRepository.deleteById(id);
        }
        
        return "redirect:/admin/posts";
    }
    
    @GetMapping("/users/{id}/edit-organization")
    public String editOrganizationForm(@PathVariable Long id, Model model) {
        Optional<com.residuosolido.app.model.User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent() && userOpt.get().getRole() == com.residuosolido.app.model.Role.ORGANIZATION) {
            model.addAttribute("user", userOpt.get());
            return "admin/edit-organization";
        }
        
        return "redirect:/users";
    }
    
    @PostMapping("/users/{id}/update-organization")
    public String updateOrganization(@PathVariable Long id, @RequestParam String email, 
                                   @RequestParam String firstName, @RequestParam String lastName) {
        Optional<com.residuosolido.app.model.User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent() && userOpt.get().getRole() == com.residuosolido.app.model.Role.ORGANIZATION) {
            com.residuosolido.app.model.User user = userOpt.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepository.save(user);
        }
        
        return "redirect:/users";
    }
    
    @GetMapping("/organizations")
    public String listOrganizations(Model model) {
        List<com.residuosolido.app.model.User> organizations = userRepository.findByRole(com.residuosolido.app.model.Role.ORGANIZATION);
        model.addAttribute("users", organizations);
        return "admin/organizations";
    }
    
    // Método público para obtener posts (usado por otros controladores)
    public static List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }
    
    public static String getCategoryName(Long categoryId) {
        // Método temporal - debería inyectar repository
        return "Categoría";
    }
}