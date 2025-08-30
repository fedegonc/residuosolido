package com.residuosolido.app.repository;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByCategory(Category category);
    List<Post> findByCategoryOrderByIdAsc(Category category);
    List<Post> findTop5ByOrderByIdDesc();
    List<Post> findTop5ByOrderByIdAsc();
    
    // Método optimizado usando JOIN FETCH para evitar N+1 queries
    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.category ORDER BY p.id ASC")
    List<Post> findFirst5WithCategories();
    
    boolean existsByCategory(Category category);
    
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findAllOrderedByIdDesc();
    
    // Método optimizado para cargar todos los posts con sus categorías
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.category ORDER BY p.id ASC")
    List<Post> findAllWithCategories();

    // Método optimizado para cargar posts recientes con categorías (INDEX)
    @Query(value = "SELECT p FROM Post p LEFT JOIN FETCH p.category ORDER BY p.id DESC", nativeQuery = false)
    List<Post> findRecentPostsWithCategories(int limit);
}
