package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryService categoryService;

    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        // Agregar nombre de categoría a cada post
        for (Post post : posts) {
            if (post.getCategoryId() != null) {
                categoryService.getCategoryById(post.getCategoryId())
                    .ifPresent(category -> post.setCategoryName(category.getName()));
            }
        }
        return posts;
    }

    public List<Post> getFirst5Posts() {
        return postRepository.findTop5ByOrderByIdDesc();
    }

    public boolean hasMoreThan5Posts() {
        return postRepository.count() > 5;
    }

    public List<Post> getPostsByCategoryId(Long categoryId) {
        return postRepository.findByCategoryId(categoryId);
    }

    public List<Post> getRelatedPostsById(Long postId, Long categoryId, int limit) {
        return postRepository.findByCategoryId(categoryId).stream()
                .filter(p -> !p.getId().equals(postId))
                .limit(limit)
                .toList();
    }

    public void createPost(String title, String content, String imageUrl, Long categoryId) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setCategoryId(categoryId);
        postRepository.save(post);
    }
    
    public void createPost(String title, String content, String imageUrl, Long categoryId, String sourceUrl, String sourceName) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setCategoryId(categoryId);
        post.setSourceUrl(sourceUrl);
        post.setSourceName(sourceName);
        postRepository.save(post);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public void updatePost(Long id, String title, String content, String imageUrl, Long categoryId) {
        postRepository.findById(id).ifPresent(post -> {
            post.setTitle(title);
            post.setContent(content);
            post.setImageUrl(imageUrl);
            post.setCategoryId(categoryId);
            postRepository.save(post);
        });
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public boolean isCategoryInUse(Long categoryId) {
        return postRepository.existsByCategoryId(categoryId);
    }
}
