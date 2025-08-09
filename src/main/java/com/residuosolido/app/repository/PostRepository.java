package com.residuosolido.app.repository;

import com.residuosolido.app.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategoryId(Long categoryId);
    List<Post> findByCategoryIdOrderByIdAsc(Long categoryId);
    List<Post> findTop5ByOrderByIdDesc();
    List<Post> findTop5ByOrderByIdAsc();
    boolean existsByCategoryId(Long categoryId);
    
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findAllOrderedByIdDesc();
    
    @Query("SELECT p FROM Post p ORDER BY p.id ASC")
    List<Post> findAllOrderedByIdAsc();
}
