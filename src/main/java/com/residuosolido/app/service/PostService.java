package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final List<Post> posts = new ArrayList<>();
    private Long nextId = 1L;

    public PostService() {
        // Datos de prueba iniciales
        posts.add(new Post(nextId++, "Campaña de Reciclaje 2025", "Únete a nuestra campaña de reciclaje", "https://via.placeholder.com/400x200", 1L));
        posts.add(new Post(nextId++, "Taller de Compostaje", "Aprende a compostar en casa", "https://via.placeholder.com/400x200", 2L));
    }

    public List<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    public void createPost(String title, String content, String imageUrl, Long categoryId) {
        posts.add(new Post(nextId++, title, content, imageUrl, categoryId));
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
