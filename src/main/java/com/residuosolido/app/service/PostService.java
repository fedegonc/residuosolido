package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.PostRepository;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.ui.Model;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    public List<Post> getAllPosts() {
        return postRepository.findAllWithCategories();
    }
    
    public List<Post> getAllPostsWithCategories() {
        // Agregar JOIN FETCH para evitar N+1 queries
        List<Post> list = postRepository.findAllWithCategories();
        if (log.isDebugEnabled()) {
            log.debug("[PostService] getAllPostsWithCategories -> {} posts", (list != null ? list.size() : 0));
        }
        return list;
    }

    public List<Post> findAllWithCategories() {
        return postRepository.findAllWithCategories();
    }

    public List<Post> findAll() {
        return postRepository.findAllWithCategories();
    }
    
    /**
     * Retorna todos los posts con sus categorías
     */
    public List<Post> findAllActive() {
        return postRepository.findAllWithCategories();
    }

    public long count() {
        return postRepository.count();
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    public List<Post> getRecentPosts(int limit) {
        // Optimización crítica: Solo cargar posts recientes con categorías
        // Usar JOIN FETCH para evitar N+1 queries en el index
        List<Post> posts = postRepository.findRecentPostsWithCategories(limit);
        if (log.isDebugEnabled()) {
            log.debug("[PostService] getRecentPosts -> {} posts (limit: {})", posts.size(), limit);
        }
        return posts;
    }

    public boolean hasMoreThan5Posts() {
        return postRepository.count() > 5;
    }

    public List<Post> getPostsByCategoryId(Long categoryId) {
        return categoryService.getCategoryById(categoryId)
            .map(postRepository::findByCategoryOrderByIdAsc)
            .orElse(new ArrayList<>());
    }

    public List<Post> getRelatedPostsById(Long postId, Long categoryId, int limit) {
        return categoryService.getCategoryById(categoryId)
            .map(postRepository::findByCategory)
            .orElse(new ArrayList<>())
            .stream()
            .filter(p -> !p.getId().equals(postId))
            .limit(limit)
            .toList();
    }

    public List<Post> getFeaturedPosts() {
        // Retornar los primeros 5 posts como "destacados"
        return postRepository.findRecentPostsWithCategories(5);
    }
    
    public List<Post> findRecentPosts(int limit) {
        return postRepository.findRecentPostsWithCategories(limit);
    }

    public List<Post> getRelatedPosts(Post post, int limit) {
        if (post == null || post.getCategory() == null) {
            return new ArrayList<>();
        }
        return getRelatedPostsById(post.getId(), post.getCategory().getId(), limit);
    }

    public void createPost(String title, String content, String imageUrl, Long categoryId) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        categoryService.getCategoryById(categoryId).ifPresent(post::setCategory);
        postRepository.save(post);
    }
    
    public void createPost(String title, String content, String imageUrl, Long categoryId, String sourceUrl, String sourceName) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        categoryService.getCategoryById(categoryId).ifPresent(post::setCategory);
        post.setSourceUrl(sourceUrl);
        post.setSourceName(sourceName);
        postRepository.save(post);
    }
    
    // Método con lógica de subida de imagen
    public void createPostWithImage(String title, String content, String imageUrl, MultipartFile imageFile, 
                                   Long categoryId, String sourceUrl, String sourceName) {
        String finalImageUrl = processImageUpload(imageUrl, imageFile);
        createPost(title, content, finalImageUrl, categoryId, sourceUrl, sourceName);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public void updatePost(Long id, String title, String content, String imageUrl, Long categoryId,
                           String sourceUrl, String sourceName) {
        postRepository.findById(id).ifPresent(post -> {
            post.setTitle(title);
            post.setContent(content);
            post.setImageUrl(imageUrl);
            categoryService.getCategoryById(categoryId).ifPresent(post::setCategory);
            post.setSourceUrl(sourceUrl);
            post.setSourceName(sourceName);
            postRepository.save(post);
        });
    }
    
    // Método con lógica de subida de imagen para actualización
    public void updatePostWithImage(Long id, String title, String content, String imageUrl, 
                                   MultipartFile imageFile, Long categoryId,
                                   String sourceUrl, String sourceName) {
        String finalImageUrl = processImageUpload(imageUrl, imageFile);
        updatePost(id, title, content, finalImageUrl, categoryId, sourceUrl, sourceName);
    }
    
    // Método para procesar la subida de imágenes
    private String processImageUpload(String imageUrl, MultipartFile imageFile) {
        String finalImageUrl = imageUrl;
        
        // Si se sube un archivo, usar Cloudinary
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                finalImageUrl = cloudinaryService.uploadFile(imageFile);
            } catch (Exception e) {
                // Si falla la subida, mantener la URL original
                System.err.println("Error subiendo imagen: " + e.getMessage());
            }
        }
        
        return finalImageUrl;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public String savePost(Post post) {
        if (post.getId() != null) {
            getPostById(post.getId()).ifPresent(existing -> {
                existing.setTitle(post.getTitle());
                existing.setContent(post.getContent());
                existing.setImageUrl(post.getImageUrl());
                existing.setCategory(post.getCategory());
                existing.setSourceUrl(post.getSourceUrl());
                existing.setSourceName(post.getSourceName());
                save(existing);
            });
            return "Post actualizado correctamente";
        } else {
            save(post);
            return "Post creado correctamente";
        }
    }

    public void preparePostListView(Model model) {
        List<Post> posts = getAllPostsWithCategories();
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("posts", posts);
        model.addAttribute("totalPosts", posts.size());
        model.addAttribute("categories", categories);
        model.addAttribute("viewType", "list");
    }

    public void preparePostFormView(Model model, Long postId, boolean isEdit) {
        List<Category> categories = categoryService.findAll();
        
        model.addAttribute("categories", categories);
        model.addAttribute("viewType", "form");
        model.addAttribute("isEdit", isEdit);
        
        if (isEdit && postId != null) {
            Post post = getPostById(postId).orElse(null);
            if (post != null) {
                model.addAttribute("post", post);
            } else {
                model.addAttribute("errorMessage", "Post no encontrado");
                preparePostListView(model);
            }
        } else {
            model.addAttribute("post", new Post());
        }
    }

    /**
     * Cuenta el número de posts asociados a una categoría
     * @param categoryId ID de la categoría
     * @return Número de posts asociados a la categoría
     */
    public long countPostsByCategory(Long categoryId) {
        return categoryService.getCategoryById(categoryId)
            .map(category -> postRepository.countByCategory(category))
            .orElse(0L);
    }

    public boolean isCategoryInUse(Long categoryId) {
        return categoryService.getCategoryById(categoryId)
            .map(postRepository::existsByCategory)
            .orElse(false);
    }
}
