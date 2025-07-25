package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final List<Post> posts = new ArrayList<>();
    private Long nextId = 1L;

    public PostService() {
        // Constructor vacío - los posts se crearán manualmente desde el admin
    }

    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    public List<Post> getFirst5Posts() {
        return posts.stream().limit(5).collect(java.util.stream.Collectors.toList());
    }

    public boolean hasMoreThan5Posts() {
        return posts.size() > 5;
    }

    // Reescrito para resolver el problema de método duplicado
    public List<Post> getPostsByCategoryId(Long categoryId) {
        return posts.stream()
                .filter(post -> post.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    // Reescrito para resolver el problema de método duplicado
    public List<Post> getRelatedPostsById(Long postId, Long categoryId, int limit) {
        return posts.stream()
                .filter(p -> p.getCategoryId().equals(categoryId) && !p.getId().equals(postId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void createPost(String title, String content, String imageUrl, Long categoryId) {
        posts.add(new Post(nextId++, title, content, imageUrl, categoryId));
    }
    
    public void createPost(String title, String content, String imageUrl, Long categoryId, String sourceUrl, String sourceName) {
        posts.add(new Post(nextId++, title, content, imageUrl, categoryId, sourceUrl, sourceName));
    }

    public Optional<Post> getPostById(Long id) {
        return posts.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void updatePost(Long id, String title, String content, String imageUrl, Long categoryId) {
        getPostById(id).ifPresent(post -> {
            post.setTitle(title);
            post.setContent(content);
            post.setImageUrl(imageUrl);
            post.setCategoryId(categoryId);
        });
    }

    public void deletePost(Long id) {
        posts.removeIf(p -> p.getId().equals(id));
    }

    public boolean isCategoryInUse(Long categoryId) {
        return posts.stream().anyMatch(p -> p.getCategoryId().equals(categoryId));
    }
}
