package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.PostRepository;
import com.residuosolido.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    public List<Post> getAllPosts() {
        return postRepository.findAllOrderedByIdAsc();
    }
    
    public List<Post> getAllPostsWithCategories() {
        List<Post> posts = postRepository.findAllOrderedByIdAsc();
        
        // Cargar todas las categorías de una vez
        List<Category> allCategories = categoryService.getAllCategories();
        Map<Long, String> categoryMap = allCategories.stream()
            .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // Asignar nombres de categoría
        posts.forEach(post -> {
            if (post.getCategoryId() != null) {
                post.setCategoryName(categoryMap.get(post.getCategoryId()));
            }
        });
        
        return posts;
    }

    public List<Post> getFirst5Posts() {
        List<Post> posts = postRepository.findTop5ByOrderByIdAsc();
        // Enriquecer con nombres de categoría para UI
        List<Category> allCategories = categoryService.getAllCategories();
        Map<Long, String> categoryMap = allCategories.stream()
            .collect(Collectors.toMap(Category::getId, Category::getName));
        posts.forEach(post -> {
            if (post.getCategoryId() != null) {
                post.setCategoryName(categoryMap.get(post.getCategoryId()));
            }
        });
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

    public boolean isCategoryInUse(Long categoryId) {
        return categoryService.getCategoryById(categoryId)
            .map(postRepository::existsByCategory)
            .orElse(false);
    }
}
