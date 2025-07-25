package com.residuosolido.app.repository;

import com.residuosolido.app.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategoryId(Long categoryId);
    List<Post> findTop5ByOrderByIdDesc();
    boolean existsByCategoryId(Long categoryId);
}
