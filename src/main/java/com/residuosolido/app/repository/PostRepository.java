package com.residuosolido.app.repository;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByCategory(Category category);
    List<Post> findByCategoryOrderByIdAsc(Category category);
    long countByCategory(Category category);
    boolean existsByCategory(Category category);
    List<Post> findAllByOrderByIdAsc();
    List<Post> findAllByOrderByIdDesc();

    default List<Post> findRecentPostsWithCategories(int limit) {
        return findAllByOrderByIdDesc().stream().limit(limit).toList();
    }

    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrCategoryNameContainingIgnoreCaseOrderByIdDesc(String title, String content, String categoryName);
}
