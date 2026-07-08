package com.residuosolido.app.service;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryService categoryService;

    public List<Post> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return postRepository.findAllByOrderByIdAsc();
        }
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrCategoryNameContainingIgnoreCaseOrderByIdDesc(query.trim(), query.trim(), query.trim());
    }

    public List<Post> getRecentPosts(int limit) {
        return postRepository.findRecentPostsWithCategories(limit);
    }

    public List<Post> getPostsByCategoryId(String categoryId) {
        return categoryService.getCategoryById(categoryId)
            .map(postRepository::findByCategoryOrderByIdAsc)
            .orElse(new ArrayList<>());
    }

    public List<Post> getRelatedPosts(Post post, int limit) {
        if (post == null || post.getCategory() == null) {
            return new ArrayList<>();
        }
        return categoryService.getCategoryById(post.getCategory().getId())
            .map(postRepository::findByCategory)
            .orElse(new ArrayList<>())
            .stream()
            .filter(p -> !p.getId().equals(post.getId()))
            .limit(limit)
            .toList();
    }

    public Optional<Post> getPostById(String id) {
        return postRepository.findById(id);
    }

 }