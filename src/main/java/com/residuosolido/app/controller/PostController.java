package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Post Controller - Controlador unificado para gestión de posts
 * 
 * Estructura:
 * - /posts - Endpoint público (solo lectura)
 * - /posts/{id} - Detalle de post
 * - /posts/category/{id} - Posts por categoría
 * - /posts/recent - Posts recientes
 * - /posts/featured - Posts destacados
 * - /admin/posts - Admin (CRUD completo)
 */
@Controller
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de posts
     */
    private void preparePostModel(Model model, List<Post> posts, String viewType) {
        model.addAttribute("posts", posts);
        model.addAttribute("totalPosts", posts.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de posts
     */
    private void handlePostError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Gestiona posts (admin)
     * Soporta vistas: list, form, view
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/posts")
    public String adminPosts(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "q", required = false) String query,
            Model model) {
        
        // Valor por defecto: lista
        String viewType = "list";
        Post post = new Post();
        
        // Determinar tipo de vista según acción
        if ("new".equals(action)) {
            viewType = "form";
            // post ya está inicializado como nuevo
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            post = postService.getPostById(id).orElse(new Post());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            post = postService.getPostById(id).orElse(new Post());
        }
        
        // Preparar modelo común
        List<Post> allPosts = postService.findAll();
        
        // Aplicar filtro de búsqueda si hay query
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            allPosts = allPosts.stream().filter(p -> {
                String title = p.getTitle() != null ? p.getTitle().toLowerCase() : "";
                String content = p.getContent() != null ? p.getContent().toLowerCase() : "";
                String categoryName = p.getCategory() != null && p.getCategory().getName() != null ? p.getCategory().getName().toLowerCase() : "";
                String created = p.getCreatedAt() != null ? p.getCreatedAt().toString().toLowerCase() : "";
                return title.contains(q) || content.contains(q) || categoryName.contains(q) || created.contains(q);
            }).toList();
        }
        
        preparePostModel(model, allPosts, viewType);
        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("query", query);
        
        return "admin/posts";
    }

    /**
     * Procesa operaciones CRUD de posts (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts")
    public String adminSavePost(
            @RequestParam(value = "action", required = false) String action,
            @ModelAttribute Post post,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Operación según acción
            if ("delete".equals(action)) {
                postService.deleteById(post.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Post eliminado correctamente");
            } else {
                // Crear o actualizar
                boolean isNew = post.getId() == null;
                
                // Manejar imagen si se subió
                if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    post.setImageUrl(imageUrl);
                }
                
                // Guardar post
                postService.save(post);
                String message = isNew ? "Post creado correctamente" : "Post actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            handlePostError(e, redirectAttributes, "Error al procesar post");
        }
        
        return "redirect:/admin/posts";
    }

    // ========== HTMX ENDPOINTS ==========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/posts/form-demo")
    public String getPostFormDemo(Model model) {
        Post demoPost = new Post();
        demoPost.setTitle("Cómo reducir residuos en el hogar");
        demoPost.setContent("En la actualidad, la gestión adecuada de los residuos sólidos se ha convertido en un tema de vital importancia para el cuidado del medio ambiente. Implementar prácticas sostenibles en nuestros hogares puede marcar una gran diferencia en la reducción de la contaminación y la conservación de recursos naturales.\n\nUna de las estrategias más efectivas es la separación de residuos en origen. Esto implica clasificar los materiales reciclables desde el momento en que se generan, facilitando así su posterior tratamiento y reutilización. Papel, cartón, plástico, vidrio y metal son algunos de los materiales que pueden ser reciclados fácilmente.\n\nOtra práctica recomendada es la reducción del consumo de productos desechables. Optar por alternativas reutilizables como bolsas de tela, botellas de agua recargables y contenedores para alimentos puede disminuir significativamente la generación de residuos.\n\nLa compostación de residuos orgánicos es también una excelente opción para quienes tienen espacio disponible. Este proceso permite transformar restos de alimentos y jardín en abono natural, reduciendo la cantidad de basura destinada a vertederos.\n\nImplementar estas medidas no solo contribuye al cuidado del planeta, sino que también puede generar ahorros económicos a largo plazo. Cada pequeño cambio cuenta en la construcción de un futuro más sostenible.");
        demoPost.setSourceUrl("https://www.epa.gov/recycle/reducing-waste-what-you-can-do");
        demoPost.setSourceName("Environmental Protection Agency");

        model.addAttribute("post", demoPost);
        model.addAttribute("categories", categoryService.findAll());
        return "admin/posts :: postFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts/form-demo")
    public String createPostDemo(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        try {
            // Crear post de prueba con datos demo
            Post demoPost = new Post();
            demoPost.setTitle("Post de Prueba - Gestión de Residuos");
            demoPost.setContent("Este es un post de prueba creado desde el botón 'Completar campos'. Contiene información sobre buenas prácticas para la gestión sostenible de residuos sólidos en entornos urbanos y rurales.\n\nLa correcta separación de residuos permite optimizar los procesos de reciclaje y reducir la contaminación ambiental. Implementar sistemas de recolección selectiva en comunidades contribuye significativamente a la preservación del medio ambiente.\n\nEs fundamental educar a la población sobre la importancia de estas prácticas para lograr un cambio sostenible a largo plazo.");
            demoPost.setSourceUrl("https://www.who.int/news-room/fact-sheets/detail/ambient-(outdoor)-air-quality-and-health");
            demoPost.setSourceName("Organización Mundial de la Salud");

            // Asignar primera categoría disponible si existe
            List<Category> categories = categoryService.findAll();
            if (!categories.isEmpty()) {
                demoPost.setCategory(categories.get(0));
            }

            postService.save(demoPost);

            redirectAttributes.addFlashAttribute("successMessage", "Post de prueba creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear post de prueba: " + e.getMessage());
        }

        return "redirect:/admin/posts";
    }

    // ========== PUBLIC ENDPOINTS ==========
    
    /**
     * Lista todos los posts (público)
     */
    @GetMapping("/posts")
    public String publicPosts(Model model) {
        try {
            List<Post> activePosts = postService.findAllActive();
            model.addAttribute("posts", activePosts);
            model.addAttribute("categories", categoryService.findAllActive());
            return "public/posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts: " + e.getMessage());
            return "public/posts";
        }
    }

    /**
     * Muestra detalle de un post (público)
     */
    @GetMapping("/posts/{id}")
    public String publicPostDetail(@PathVariable Long id, Model model) {
        try {
            Optional<Post> postOpt = postService.getPostById(id);
            if (postOpt.isEmpty() || !postOpt.get().isActive()) {
                return "redirect:/posts";
            }
            
            Post post = postOpt.get();
            model.addAttribute("post", post);
            
            // Obtener posts relacionados (misma categoría, excluyendo el actual)
            List<Post> relatedPosts = postService.getRelatedPosts(post, 3);
            model.addAttribute("relatedPosts", relatedPosts);
            
            return "public/post-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar post: " + e.getMessage());
            return "redirect:/posts";
        }
    }

    /**
     * Lista posts por categoría (público)
     */
    @GetMapping("/posts/category/{categoryId}")
    public String publicPostsByCategory(@PathVariable Long categoryId, Model model) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty() || !categoryOpt.get().isActive()) {
                return "redirect:/categories";
            }
            
            Category category = categoryOpt.get();
            List<Post> categoryPosts = postService.getPostsByCategoryId(categoryId);
            
            model.addAttribute("category", category);
            model.addAttribute("posts", categoryPosts);
            model.addAttribute("totalPosts", categoryPosts.size());
            
            return "public/category-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts de categoría: " + e.getMessage());
            return "redirect:/categories";
        }
    }

    /**
     * Lista posts recientes (público)
     */
    @GetMapping("/posts/recent")
    public String publicRecentPosts(Model model) {
        try {
            List<Post> recentPosts = postService.getRecentPosts(5);
            model.addAttribute("posts", recentPosts);
            model.addAttribute("title", "Posts Recientes");
            return "public/recent-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts recientes: " + e.getMessage());
            return "redirect:/posts";
        }
    }

    /**
     * Lista posts destacados (público)
     */
    @GetMapping("/posts/featured")
    public String publicFeaturedPosts(Model model) {
        try {
            List<Post> featuredPosts = postService.getFeaturedPosts();
            model.addAttribute("posts", featuredPosts);
            model.addAttribute("title", "Posts Destacados");
            return "public/featured-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts destacados: " + e.getMessage());
            return "redirect:/posts";
        }
    }
    
    /**
     * API para obtener posts recientes
     */
    @GetMapping("/api/posts/recent")
    @ResponseBody
    public List<Post> getRecentPosts(@RequestParam(defaultValue = "3") int limit) {
        return postService.getRecentPosts(limit);
    }
}
